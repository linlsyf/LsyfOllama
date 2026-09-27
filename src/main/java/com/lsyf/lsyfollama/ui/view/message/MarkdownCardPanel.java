package com.lsyf.lsyfollama.ui.view.message;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.TextTransferable;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把 AI 回复渲染成卡片流：正文段落 + 独立代码块卡片（语言标签 + Copy 按钮 + 关键字着色）。
 *
 * <p><b>流式策略（刻意做得极简）</b>：流式期间整个面板只有一个 {@link PlainTextArea}，
 * 每次刷新就是一次 {@code setText(全量文本)}，不维护任何段索引 / pending / builtCount 状态。
 * 收到结束信号后一次性全量渲染成卡片。
 * 这样彻底消除增量状态错位导致的白屏、尾部丢失、内容重复。
 *
 * <p>用法：
 * <ul>
 *   <li>一次性：{@link #render(String)}</li>
 *   <li>流式：{@link #beginStream()} → {@link #append(String)} × N → {@link #endStream()}</li>
 * </ul>
 */
public class MarkdownCardPanel extends JPanel implements Disposable {

  private static final int MAX_CODE_LINES = 30;
  private static final int RADIUS = 8;
  private static final int FLUSH_DELAY_MS = 80;

  private static final JBColor CARD_BORDER =
      JBColor.namedColor("Component.borderColor", new JBColor(0xC9CCCF, 0x4A4F55));
  private static final JBColor HEADER_BG =
      JBColor.namedColor("ToolWindow.header.background", new JBColor(0xF1F2F4, 0x31363A));

  private final BoxPanel content;
  private final JBScrollPane scroll;

  /** 流式期间唯一的组件 */
  private PlainTextArea streamArea;

  private volatile String lastMarkdown = "";
  private volatile boolean disposed = false;
  private volatile boolean streaming = false;

  /** 点击「Accept」时的回调，参数为该代码块内容；为 null 时按钮点击不做任何事 */
  private volatile java.util.function.Consumer<String> acceptHandler = null;

  /** 注入 Accept 的处理逻辑，例如把代码插入编辑器 */
  public void setAcceptHandler(@Nullable java.util.function.Consumer<String> handler) {
    this.acceptHandler = handler;
  }
  private final StringBuilder streamBuf = new StringBuilder();
  private Timer flushTimer;
  private boolean stickToBottom = true;

  public MarkdownCardPanel(@NotNull Project project) {
    setLayout(new BorderLayout());
    setOpaque(true);
    setBackground(UIUtil.getPanelBackground());

    content = new BoxPanel();
    content.setOpaque(true);
    content.setBackground(UIUtil.getPanelBackground());
    content.setBorder(JBUI.Borders.empty(6));

    scroll = new JBScrollPane(content);
    scroll.setBorder(JBUI.Borders.empty());
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(16);

    // viewport 必须显式设背景，否则主题下出现灰雾 / 闪白
    scroll.setOpaque(true);
    scroll.setBackground(UIUtil.getPanelBackground());
    scroll.getViewport().setOpaque(true);
    scroll.getViewport().setBackground(UIUtil.getPanelBackground());

    add(scroll, BorderLayout.CENTER);

    scroll.getVerticalScrollBar().addAdjustmentListener(e -> {
      JScrollBar bar = scroll.getVerticalScrollBar();
      stickToBottom = bar.getValue() >= (bar.getMaximum() - bar.getVisibleAmount() - 8);
    });

    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(this)
        .subscribe(EditorColorsManager.TOPIC, new EditorColorsListener() {
          @Override
          public void globalSchemeChange(@Nullable EditorColorsScheme scheme) {
            runOnEdt(() -> {
              if (lastMarkdown.isEmpty()) return;
              if (streaming) repack();
              else render(lastMarkdown);
            });
          }
        });
  }

  // ================= 对外 API =================

  /**
   * 调试用：把文本的首尾原样打印出来（含不可见字符的 Unicode 码），
   * 用来确认多余的标点（如行首冒号、行尾句号）是渲染层加的，还是上游传进来的。
   */
  private static String dump(String t) {
    if (t == null) return "null";
    StringBuilder sb = new StringBuilder();
    sb.append("len=").append(t.length()).append(" [");
    int n = Math.min(24, t.length());
    for (int i = 0; i < n; i++) {
      char c = t.charAt(i);
      sb.append(c);
      if (c < 0x20 || c > 0x7e) sb.append("(u").append(Integer.toHexString(c)).append(")");
    }
    sb.append(" ...");
    int tailStart = Math.max(0, t.length() - 12);
    for (int i = tailStart; i < t.length(); i++) {
      char c = t.charAt(i);
      sb.append(c);
      if (c < 0x20 || c > 0x7e) sb.append("(u").append(Integer.toHexString(c)).append(")");
    }
    sb.append("]");
    return sb.toString();
  }

  /** 一次性渲染完整回复 */
  public void render(@NotNull String markdown) {
    stopFlushTimer();
    streaming = false;
    streamArea = null;
    lastMarkdown = markdown;
    System.out.println("[MD] render " + dump(markdown));

    content.removeAll();
    for (MdSegment seg : MarkdownSplitter.parse(markdown)) {
      content.add(seg.code() ? buildCodeCard(seg) : buildTextCard(seg.body()));
      content.add(Box.createVerticalStrut(6));
    }
    content.add(Box.createVerticalGlue());

    content.revalidate();
    content.repaint();
    scroll.getVerticalScrollBar().setValue(0);
  }

  /** 开始一轮流式输出 */
  public void beginStream() {
    if (disposed) return;
    streaming = true;
    stickToBottom = true;
    synchronized (streamBuf) {
      streamBuf.setLength(0);
    }
    lastMarkdown = "";

    runOnEdt(() -> {
      if (disposed) return;
      stopFlushTimer();
      streamArea = new PlainTextArea(false, "");
      content.removeAll();
      content.add(streamArea);
      content.add(Box.createVerticalGlue());
      content.revalidate();
      content.repaint();
    });
  }

  /** 追加流式内容，可在任意线程调用 */
  public void append(@NotNull String chunk) {
    if (disposed || chunk.isEmpty()) return;
    if (!streaming) beginStream();

    synchronized (streamBuf) {
      if (streamBuf.length() == 0) {
        System.out.println("[MD] firstChunk " + dump(chunk));
      }
      streamBuf.append(chunk);
    }
    runOnEdt(this::scheduleFlush);
  }

  /** 流式结束：一次性全量渲染成卡片 */
  public void endStream() {
    if (disposed) return;
    // 关键：读缓冲必须发生在这个 EDT 任务「执行时」，而不是 endStream「被调用时」。
    // append() 走的是 runOnEdt，都还排在 EDT 队列里；这里同步读会读到空/残内容。
    runOnEdt(() -> {
      if (disposed) return;
      final String text;
      synchronized (streamBuf) {
        text = streamBuf.toString();
        streamBuf.setLength(0);
      }
      streaming = false;
      lastMarkdown = text;
      System.out.println("[MD] endStream(EDT) " + dump(text));
      stopFlushTimer();
      streamArea = null;
      render(text);                 // 全量重建，不做任何增量
    });
  }

  public void clear() {
    stopFlushTimer();
    streaming = false;
    streamArea = null;
    lastMarkdown = "";
    synchronized (streamBuf) {
      streamBuf.setLength(0);
    }
    content.removeAll();
    content.add(Box.createVerticalGlue());
    content.revalidate();
    content.repaint();
  }

  public boolean isStreaming() {
    return streaming;
  }

  // ================= 流式调度 =================

  private void scheduleFlush() {
    if (disposed) return;
    if (flushTimer == null) {
      flushTimer = new Timer(FLUSH_DELAY_MS, e -> flushNow());
      flushTimer.setRepeats(false);
    }
    if (flushTimer.isRunning()) return;
    flushTimer.restart();
  }

  private void stopFlushTimer() {
    if (flushTimer != null) {
      flushTimer.stop();
      flushTimer = null;
    }
  }

  /** 流式刷新：把全量文本塞进唯一的文本区，不做任何解析 / 卡片重建 */
  private void flushNow() {
    if (disposed) return;
    if (streamArea == null) {
      // 极端情况：append 先于 beginStream 的 EDT 任务执行，这里补建
      streamArea = new PlainTextArea(false, "");
      content.removeAll();
      content.add(streamArea);
      content.add(Box.createVerticalGlue());
    }
    String text;
    synchronized (streamBuf) {
      text = streamBuf.toString();
    }
    lastMarkdown = text;
    streamArea.setText(text);
    content.revalidate();
    content.repaint();
    scrollToBottom();
  }

  /** 主题切换但仍在流式时：重建文本区即可 */
  private void repack() {
    if (streamArea != null) {
      streamArea.applyTheme(false);
    }
    content.revalidate();
    content.repaint();
  }

  private void scrollToBottom() {
    if (!stickToBottom) return;
    SwingUtilities.invokeLater(() -> {
      JScrollBar bar = scroll.getVerticalScrollBar();
      bar.setValue(bar.getMaximum());
    });
  }

  // ================= 卡片 =================

  private JComponent buildTextCard(String body) {
    PlainTextArea area = new PlainTextArea(false, body);
    area.setBackground(UIUtil.getPanelBackground());
    area.setBorder(JBUI.Borders.empty(2, 2, 4, 2));
    area.setAlignmentX(LEFT_ALIGNMENT);
    return area;
  }

  private JComponent buildCodeCard(MdSegment seg) {
    JPanel card = new JPanel(new BorderLayout(0, 0));
    card.setOpaque(true);
    card.setBackground(codeBg());
    card.setBorder(new RoundedBorder(CARD_BORDER, RADIUS));
    card.setAlignmentX(LEFT_ALIGNMENT);

    // 用 BoxLayout(X_AXIS) 而不是 BorderLayout：
    // BorderLayout 的 EAST 区在父容器宽度不足时会被压成 0 宽，Copy 按钮就消失了。
    // BoxLayout 会尊重子组件自己设定的 maximumSize，不会被压没。
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
    header.setOpaque(true);
    header.setBackground(HEADER_BG);
    header.setBorder(BorderFactory.createCompoundBorder(
        new javax.swing.border.MatteBorder(0, 0, 1, 0, CARD_BORDER),
        JBUI.Borders.empty(4, 8, 4, 6)));

    JLabel lang = new JLabel(seg.displayLang());
    lang.setFont(JBFont.label().deriveFont(Font.PLAIN, JBFont.label().getSize() - 2f));
    lang.setForeground(UIUtil.getLabelForeground());
    lang.setAlignmentY(Component.CENTER_ALIGNMENT);

    JButton copyBtn = buildCopyButton(seg.body());
    copyBtn.setAlignmentY(Component.CENTER_ALIGNMENT);

    // Accept 放在 Copy 右侧，始终创建 —— 不因未注入 handler 而消失
    JButton acceptBtn = buildAcceptButton(seg.body());
    acceptBtn.setAlignmentY(Component.CENTER_ALIGNMENT);

    header.add(Box.createHorizontalStrut(2));
    header.add(lang);
    header.add(Box.createHorizontalGlue());     // 把按钮推到最右
    header.add(copyBtn);
    header.add(Box.createHorizontalStrut(4));
    header.add(acceptBtn);
    header.add(Box.createHorizontalStrut(2));

    // 固定 header 高度
    int headerH = Math.max(32, copyBtn.getPreferredSize().height + 8);
    header.setPreferredSize(new Dimension(Integer.MAX_VALUE, headerH));
    header.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerH));
    header.setMinimumSize(new Dimension(0, headerH));

    card.add(header, BorderLayout.NORTH);

    CodePane pane = new CodePane(seg.displayLang(), seg.body());
    JBScrollPane codeScroll = new JBScrollPane(pane);
    codeScroll.setBorder(JBUI.Borders.empty());
    codeScroll.setOpaque(true);
    codeScroll.setBackground(codeBg());
    codeScroll.getViewport().setOpaque(true);
    codeScroll.getViewport().setBackground(codeBg());
    codeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    codeScroll.setVerticalScrollBarPolicy(
        pane.getRowCount() > MAX_CODE_LINES
            ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    codeScroll.setAlignmentX(LEFT_ALIGNMENT);

    int h = pane.getFixedHeight();
    codeScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, h));
    codeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    codeScroll.setMinimumSize(new Dimension(0, h));

    card.add(codeScroll, BorderLayout.CENTER);

    // 关键：不要用 card.getPreferredSize() —— 此时卡片还没进容器、布局未生效，
    // 读到的高度偏小，BoxLayout 会按 maximumSize 裁剪，header（含 Copy 按钮）被裁掉。
    // 改成逐段相加，再加边框 insets。
    Insets bi = card.getBorder().getBorderInsets(card);
    int cardH = headerH + h + bi.top + bi.bottom + 2;

    card.setPreferredSize(new Dimension(Integer.MAX_VALUE, cardH));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardH));
    card.setMinimumSize(new Dimension(0, cardH));

    // 兜底路径：右键菜单一定可用，不依赖按钮是否可见
    attachCopyPopup(card, seg.body());
    attachCopyPopup(codeScroll, seg.body());
    attachCopyPopup(pane, seg.body());

    // 诊断：布局生效后再打印一次真实尺寸，确认 header / 按钮是否真的有尺寸
    SwingUtilities.invokeLater(() -> {
      System.out.println("[CARD] cardH=" + cardH + " headerH=" + headerH + " codeH=" + h
          + " | card=" + card.getBounds()
          + " header=" + header.getBounds()
          + " btn=" + copyBtn.getBounds()
          + " btnVisible=" + copyBtn.isVisible()
          + " btnShowing=" + copyBtn.isShowing()
          + " icon=" + copyBtn.getIcon()
          + " text=" + copyBtn.getText());
    });

    return card;
  }

  /** 给任意组件挂上「复制代码 / 接受代码」右键菜单 */
  private void attachCopyPopup(@NotNull JComponent target, @NotNull String code) {
    JPopupMenu menu = new JPopupMenu();

    JMenuItem copyItem = new JMenuItem("Copy Code", AllIcons.Actions.Copy);
    copyItem.addActionListener(e -> {
      if (!code.isBlank()) {
        CopyPasteManager.getInstance().setContents(new TextTransferable(code));
      }
    });
    menu.add(copyItem);

    JMenuItem acceptItem = new JMenuItem("Accept Code", AllIcons.Actions.Commit);
    acceptItem.addActionListener(e -> {
      java.util.function.Consumer<String> h = acceptHandler;
      if (h != null && !code.isBlank()) {
        try {
          h.accept(code);
        } catch (Throwable t) {
          System.out.println("[ACCEPT] failed: " + t);
        }
      }
    });
    menu.add(acceptItem);

    target.setComponentPopupMenu(menu);
  }

  /**
   * 「Accept」按钮：位于 Copy 右侧，点击后把代码块交给外层处理。
   * 未注入 handler 时按钮依然显示，只是点击后无动作。
   */
  private JButton buildAcceptButton(String code) {
    Icon icon = AllIcons.Actions.Commit;
    JButton btn = new JButton("Accept", icon);
    btn.setToolTipText("Accept this code block");
    btn.setFont(JBFont.label().deriveFont(Font.PLAIN, JBFont.label().getSize() - 2f));
    btn.setForeground(UIUtil.getLabelForeground());
    btn.setBorderPainted(true);
    btn.setContentAreaFilled(true);
    btn.setFocusPainted(false);
    btn.setFocusable(false);
    btn.setRequestFocusEnabled(false);
    btn.setOpaque(true);
    btn.setBackground(HEADER_BG);
    btn.setMargin(JBUI.insets(2, 10, 2, 10));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // 固定尺寸，避免被 BoxLayout 压成 0 宽
    Dimension bs = new Dimension(Math.max(84, btn.getPreferredSize().width), 26);
    btn.setPreferredSize(bs);
    btn.setMaximumSize(bs);
    btn.setMinimumSize(bs);
    btn.setSize(bs);

    btn.addActionListener(e -> {
      java.util.function.Consumer<String> h = acceptHandler;
      if (h == null || code.isBlank()) return;
      try {
        h.accept(code);
        btn.setText("Accepted");
        javax.swing.Timer t = new javax.swing.Timer(1200, ev -> btn.setText("Accept"));
        t.setRepeats(false);
        t.start();
      } catch (Throwable t) {
        System.out.println("[ACCEPT] failed: " + t);
      }
    });
    return btn;
  }

  private JButton buildCopyButton(String code) {
    Icon icon = AllIcons.Actions.Copy;
    JButton btn = new JButton("Copy", icon);   // 图标为 null 时靠文字兜底
    btn.setToolTipText("Copy code");
    btn.setFont(JBFont.label().deriveFont(Font.PLAIN, JBFont.label().getSize() - 2f));
    btn.setForeground(UIUtil.getLabelForeground());
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setFocusPainted(false);
    btn.setFocusable(false);
    btn.setRequestFocusEnabled(false);
    btn.setOpaque(true);
    btn.setBackground(HEADER_BG);
    btn.setMargin(JBUI.insets(2, 8, 2, 8));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // 固定尺寸：BorderLayout 的 EAST 区在父容器宽度不足时会被压成 0 宽
    Dimension bs = new Dimension(Math.max(76, btn.getPreferredSize().width), 26);
    btn.setPreferredSize(bs);
    btn.setMaximumSize(bs);
    btn.setMinimumSize(bs);
    btn.setSize(bs);
    btn.addActionListener(e -> {
      if (!code.isBlank()) {
        CopyPasteManager.getInstance().setContents(new TextTransferable(code));
      }
      btn.setText("Copied");
      Timer t = new Timer(1200, ev -> btn.setText("Copy"));
      t.setRepeats(false);
      t.start();
    });
    return btn;
  }

  // ================= 代码块（纯 Swing，无 Editor 组件） =================

  private static final class CodePane extends JTextPane implements Scrollable {

    private final int rowCount;
    private final int fixedHeight;

    CodePane(String langTag, String text) {
      setEditable(false);
      setOpaque(true);
      setBackground(codeBg());
      setBorder(JBUI.Borders.empty(4, 10, 6, 10));
      setEditorKit(new NoWrapStyledEditorKit());

      EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
      Font base = new Font(scheme.getEditorFontName(), Font.PLAIN, scheme.getEditorFontSize());
      setFont(base);
      setForeground(scheme.getDefaultForeground());
      setCaretColor(scheme.getDefaultForeground());
      putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

      CodeHighlight.apply(getStyledDocument(), text, CodeHighlight.keywordsOf(langTag), scheme);

      rowCount = countLines(text);
      FontMetrics fm = getFontMetrics(base);
      int shown = Math.min(rowCount, MAX_CODE_LINES);
      fixedHeight = fm.getHeight() * Math.max(1, shown) + 14 + 10;
      setPreferredSize(new Dimension(Integer.MAX_VALUE, fixedHeight));
    }

    int getRowCount() { return rowCount; }
    int getFixedHeight() { return fixedHeight; }

    @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, fixedHeight); }
    @Override public Dimension getMinimumSize() { return new Dimension(0, fixedHeight); }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(16, r.height); }
    @Override public boolean getScrollableTracksViewportWidth() { return false; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
  }

  /** 让 JTextPane 横向滚动而不是折行 */
  private static final class NoWrapStyledEditorKit extends StyledEditorKit {
    private final ViewFactory base = new StyledEditorKit().getViewFactory();

    @Override public ViewFactory getViewFactory() {
      return elem -> {
        if (AbstractDocument.SectionElementName.equals(elem.getName())) {
          return new BoxView(elem, View.Y_AXIS) {
            @Override public float getMinimumSpan(int axis) { return getPreferredSpan(axis); }
            @Override public float getPreferredSpan(int axis) {
              return axis == View.X_AXIS ? super.getMaximumSpan(axis) : super.getPreferredSpan(axis);
            }
            @Override public void layout(int w, int h) { super.layout(Short.MAX_VALUE, h); }
          };
        }
        return base.create(elem);
      };
    }
  }

  // ================= 轻量关键字着色 =================

  private static final class CodeHighlight {

    private static final Pattern TOKEN = Pattern.compile(
        "(//[^\\n]*|/\\*[\\s\\S]*?\\*/)"
            + "|(\"(?:[^\"\\\\\\n]|\\\\.)*\"|'(?:[^'\\\\\\n]|\\\\.)*')"
            + "|(\\b\\d+(?:\\.\\d+)?[fFlLdD]?\\b)"
            + "|([A-Za-z_$][A-Za-z0-9_$]*)"
    );

    private static final Set<String> JAVA = set("abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while var record true false null");
    private static final Set<String> PY = set("and as assert async await break class continue def del elif else except finally for from global if import in is lambda nonlocal not or pass raise return try while with yield True False None self");
    private static final Set<String> JS = set("break case catch class const continue debugger default delete do else export extends finally for function if import in instanceof let new return super switch this throw try typeof var void while with yield async await true false null undefined");
    private static final Set<String> GO = set("break case chan const continue default defer else fallthrough for func go goto if import interface map package range return select struct switch type var true false nil");
    private static final Set<String> RS = set("as async await break const continue crate dyn else enum extern false fn for if impl in let loop match mod move mut pub ref return self Self static struct super trait true type unsafe use where while");
    private static final Set<String> SQL = set("select from where insert into values update set delete create table drop alter add primary key foreign references join left right inner outer on group by order having limit offset as distinct and or not null is in like between union all");
    private static final Set<String> KT = set("abstract actual as break by catch class companion const constructor continue crossinline data do dynamic else enum expect external false final finally for fun get if import infix init inline interface internal is lateinit noinline null object open operator out override package private protected public reified return sealed set super suspend tailrec this throw true try typealias val var vararg when where while");
    private static final Set<String> CS = set("abstract as base bool break byte case catch char checked class const continue decimal default delegate do double else enum event explicit extern false finally fixed float for foreach goto if implicit in int interface internal is lock long namespace new null object operator out override params private protected public readonly ref return sbyte sealed short sizeof stackalloc static string struct switch this throw true try typeof uint ulong unchecked unsafe ushort using var virtual void volatile while");
    private static final Set<String> SH = set("if then else elif fi for while do done case esac function in until select return local export echo cd source exit set unset trap");

    private static final Map<String, Set<String>> BY_LANG = new HashMap<>();
    static {
      BY_LANG.put("java", JAVA);
      BY_LANG.put("kotlin", KT);
      BY_LANG.put("kt", KT);
      BY_LANG.put("python", PY);
      BY_LANG.put("py", PY);
      BY_LANG.put("javascript", JS);
      BY_LANG.put("js", JS);
      BY_LANG.put("typescript", JS);
      BY_LANG.put("ts", JS);
      BY_LANG.put("vue", JS);
      BY_LANG.put("json", JS);
      BY_LANG.put("go", GO);
      BY_LANG.put("golang", GO);
      BY_LANG.put("rust", RS);
      BY_LANG.put("rs", RS);
      BY_LANG.put("sql", SQL);
      BY_LANG.put("csharp", CS);
      BY_LANG.put("cs", CS);
      BY_LANG.put("sh", SH);
      BY_LANG.put("bash", SH);
      BY_LANG.put("zsh", SH);
      BY_LANG.put("shell script", SH);
    }

    static Set<String> keywordsOf(String tag) {
      Set<String> kw = BY_LANG.get(tag == null ? "" : tag.toLowerCase(Locale.ROOT));
      return kw == null ? Collections.emptySet() : kw;
    }

    static void apply(StyledDocument doc, String text, Set<String> keywords, EditorColorsScheme scheme) {
      Color cKeyword = attrColor(scheme, "DEFAULT_KEYWORD", new JBColor(0x0033B3, 0xCC7832));
      Color cString = attrColor(scheme, "DEFAULT_STRING", new JBColor(0x067D17, 0x6A8759));
      Color cNumber = attrColor(scheme, "DEFAULT_NUMBER", new JBColor(0x1750EB, 0x6897BB));
      Color cComment = attrColor(scheme, "LINE_COMMENT", new JBColor(0x8C8C8C, 0x808080));
      Color cDefault = scheme.getDefaultForeground();

      SimpleAttributeSet def = attrs(cDefault);
      SimpleAttributeSet kwA = attrs(cKeyword);
      SimpleAttributeSet str = attrs(cString);
      SimpleAttributeSet num = attrs(cNumber);
      SimpleAttributeSet cmt = attrs(cComment);

      try {
        doc.remove(0, doc.getLength());
        Matcher m = TOKEN.matcher(text);
        int pos = 0;
        while (m.find()) {
          if (m.start() > pos) {
            doc.insertString(doc.getLength(), text.substring(pos, m.start()), def);
          }
          String tok = m.group();
          SimpleAttributeSet a;
          if (m.group(1) != null) a = cmt;
          else if (m.group(2) != null) a = str;
          else if (m.group(3) != null) a = num;
          else a = keywords.contains(tok) ? kwA : def;
          doc.insertString(doc.getLength(), tok, a);
          pos = m.end();
        }
        if (pos < text.length()) {
          doc.insertString(doc.getLength(), text.substring(pos), def);
        }
      } catch (BadLocationException ignored) {
        // 高亮失败不影响文本显示
      }
    }

    private static SimpleAttributeSet attrs(Color c) {
      SimpleAttributeSet s = new SimpleAttributeSet();
      StyleConstants.setForeground(s, c);
      return s;
    }

    private static Color attrColor(EditorColorsScheme scheme, String keyName, Color fallback) {
      try {
        TextAttributesKey key = TextAttributesKey.find(keyName);
        if (key != null) {
          TextAttributes ta = scheme.getAttributes(key);
          if (ta != null && ta.getForegroundColor() != null) return ta.getForegroundColor();
        }
      } catch (Throwable ignored) { /* 用回退色 */ }
      return fallback;
    }

    private static Set<String> set(String s) {
      return new HashSet<>(Arrays.asList(s.split(" ")));
    }
  }

  // ================= 正文 / 流式文本区 =================

  /**
   * 只读文本区：正文与流式输出共用。
   * 高度按父容器可用宽度手动量，保证 BoxLayout 下不塌陷成 0 高度（白屏常见成因）。
   */
  private static final class PlainTextArea extends JTextArea {

    private boolean code;

    PlainTextArea(boolean isCode, String text) {
      super(text);
      this.code = isCode;
      setEditable(false);
      setOpaque(true);
      setRequestFocusEnabled(false);
      applyTheme(isCode);
    }

    void applyTheme(boolean isCode) {
      this.code = isCode;
      setLineWrap(!isCode);
      setWrapStyleWord(!isCode);
      setBorder(JBUI.Borders.empty(2, 4, 4, 6));
      if (isCode) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        setFont(new Font(scheme.getEditorFontName(), Font.PLAIN, scheme.getEditorFontSize()));
        setForeground(scheme.getDefaultForeground());
        setBackground(codeBg());
      } else {
        setFont(JBFont.label());
        setForeground(UIUtil.getLabelForeground());
        setBackground(UIUtil.getPanelBackground());
      }
    }

    @Override public Dimension getPreferredSize() {
      int w = getParent() == null ? 0 : getParent().getWidth();
      if (w <= 0) {
        // 宽度未知时给一个安全默认，避免 0 宽 0 高 → 白屏
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(100, d.width), Math.max(20, d.height));
      }
      Insets in = getInsets();
      FontMetrics fm = getFontMetrics(getFont());
      int innerW = Math.max(1, w - in.left - in.right);

      int lines = 0;
      for (String para : getText().split("\n", -1)) {
        if (para.isEmpty()) { lines++; continue; }
        lines += code ? 1
            : Math.max(1, (int) Math.ceil(fm.stringWidth(para) / (double) innerW));
      }
      return new Dimension(w, Math.max(20, lines * fm.getHeight() + in.top + in.bottom));
    }

    @Override public Dimension getMaximumSize() {
      return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override public Dimension getMinimumSize() {
      return new Dimension(0, getPreferredSize().height);
    }
  }

  // ================= 容器 / 边框 =================

  private static final class BoxPanel extends JPanel implements Scrollable {
    BoxPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setOpaque(true);
      setBackground(UIUtil.getPanelBackground());
    }

    /** 卡片与 Copy 按钮重叠，必须关掉优化绘制，否则滚动留残影 */
    @Override public boolean isOptimizedDrawingEnabled() { return false; }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(16, r.height); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
  }

  private static final class RoundedBorder implements Border {
    private final Color color;
    private final int radius;

    RoundedBorder(Color color, int radius) { this.color = color; this.radius = radius; }

    @Override public Insets getBorderInsets(Component c) { return new Insets(4, 2, 2, 2); }

    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.setStroke(new BasicStroke(1f));
      g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
      g2.dispose();
    }

    @Override public boolean isBorderOpaque() { return false; }
  }

  // ================= 工具 =================

  private static void runOnEdt(@NotNull Runnable r) {
    if (SwingUtilities.isEventDispatchThread()) r.run();
    else ApplicationManager.getApplication().invokeLater(r);
  }

  private static Color codeBg() {
    return EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
  }

  private static int countLines(String s) {
    if (s == null || s.isEmpty()) return 1;
    int n = 1;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '\n') n++;
    }
    return n;
  }

  @Override public void dispose() {
    disposed = true;
    stopFlushTimer();
    content.removeAll();
  }


  /** 最近一次渲染/流式的完整原文，供外层「接受」「重新生成」取用 */
  public String getLastMarkdown() {
    return lastMarkdown == null ? "" : lastMarkdown;
  }

  /**
   * 从任意线程安全取用：如果当前正好在 EDT 上就直接读，
   * 否则提交到 EDT 执行回调，避免读到半截内容。
   */
  public void getLastMarkdownAsync(@NotNull java.util.function.Consumer<String> callback) {
    if (SwingUtilities.isEventDispatchThread()) {
      callback.accept(getLastMarkdown());
      return;
    }
    ApplicationManager.getApplication().invokeLater(() -> callback.accept(getLastMarkdown()));
  }
}
