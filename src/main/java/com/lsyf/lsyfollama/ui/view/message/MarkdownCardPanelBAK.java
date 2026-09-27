package com.lsyf.lsyfollama.ui.view.message;

import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.TextTransferable;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 把 AI 回复渲染成卡片流：正文段落 + 独立代码块卡片（带语言标签与 Copy 按钮）。
 */
public class MarkdownCardPanelBAK extends JPanel implements Disposable {

  private static final int MAX_CODE_LINES = 30;   // 超过则卡片内部纵向滚动
  private static final int RADIUS = 8;
  private static final JBColor CARD_BORDER =
      JBColor.namedColor("Component.borderColor", new JBColor(0xC9CCCF, 0x4A4F55));

  private final Project project;
  private final ScrollableBoxPanel content;
  private final JBScrollPane scroll;
  private final List<LanguageTextField> codeFields = new ArrayList<>();

  private String lastMarkdown = "";

  public MarkdownCardPanelBAK(@NotNull Project project) {
    this.project = project;
    setLayout(new BorderLayout());

    content = new ScrollableBoxPanel();
    content.setBackground(UIUtil.getPanelBackground());
    content.setBorder(JBUI.Borders.empty(6));

    scroll = new JBScrollPane(content);
    scroll.setBorder(JBUI.Borders.empty());
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(16);

    add(scroll, BorderLayout.CENTER);

// 主题 / 配色方案切换后重绘
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(this)
        .subscribe(EditorColorsManager.TOPIC, new EditorColorsListener() {
          @Override
          public void globalSchemeChange(@Nullable EditorColorsScheme editorColorsScheme) {

            ApplicationManager.getApplication().invokeLater(() -> reRender());
          }
        });
  }

  // ---------- 对外 API ----------

  /**
   * EDT 中调用；流式场景下建议只在结束时调用一次
   */
  public void render(@NotNull String markdown) {
    lastMarkdown = markdown;
    clearContent();

    for (MdSegment seg : MarkdownSplitter.parse(markdown)) {
      content.add(seg.code() ? buildCodeCard(seg) : buildTextCard(seg.body()));
      content.add(Box.createVerticalStrut(6));
    }
    content.add(Box.createVerticalGlue());   // 吸收剩余空间，防止卡片被拉伸

    content.revalidate();
    content.repaint();
    scroll.getVerticalScrollBar().setValue(0);
    // 父容器宽度已知后再量一次，修正换行高度
    SwingUtilities.invokeLater(() -> {
      content.revalidate();
      content.repaint();
    });
  }

  public void clear() {
    lastMarkdown = "";
    clearContent();
    content.revalidate();
    content.repaint();
  }

  private void reRender() {
    if (!lastMarkdown.isEmpty()) render(lastMarkdown);
  }

  private void clearContent() {
    for (LanguageTextField f : codeFields) {
      try {
        // 从容器移除后，编辑器资源由组件自身在 removeNotify 时回收
        f.removeNotify();
      } catch (Throwable ignored) { /* ignore */ }
    }
    codeFields.clear();
    content.removeAll();
  }

  // ---------- 卡片构建 ----------

  private JComponent buildTextCard(String body) {
    WrappingTextArea area = new WrappingTextArea(body);
    area.setBackground(UIUtil.getPanelBackground());
    area.setBorder(JBUI.Borders.empty(2, 2, 4, 2));
    area.setAlignmentX(LEFT_ALIGNMENT);
    return area;
  }

  private JComponent buildCodeCard(MdSegment seg) {
    JPanel card = new JPanel(new BorderLayout(0, 4));
    card.setOpaque(true);
    card.setBackground(codeBg());
    card.setBorder(new RoundedBorder(CARD_BORDER, RADIUS));
    card.setAlignmentX(LEFT_ALIGNMENT);

    // 头部：语言标签 + Copy 按钮
    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);
    header.setBorder(JBUI.Borders.empty(4, 8, 0, 6));

    JLabel lang = new JLabel(seg.displayLang());
    lang.setFont(JBFont.label().deriveFont(Font.PLAIN, JBFont.label().getSize() - 2f));
    lang.setForeground(JBColor.namedColor("Label.disabledForeground", JBColor.GRAY));
    header.add(lang, BorderLayout.WEST);
    header.add(buildCopyButton(seg.body()), BorderLayout.EAST);

    card.add(header, BorderLayout.NORTH);

    // 代码体
    // 代码体
    CodeField field = new CodeField(resolveLanguage(seg.displayLang()), project, seg.body());

    codeFields.add(field);
    card.add(field, BorderLayout.CENTER);

    // 固定为 preferred 高度，避免被 BoxLayout 拉伸
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
    return card;
  }

  private JButton buildCopyButton(String code) {
    JButton btn = new JButton(AllIcons.Actions.Copy);
    btn.setUI(new BasicButtonUI());
    btn.setToolTipText("Copy code");
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setOpaque(false);
    btn.setFocusable(false);                    // 不抢焦点，避免打断正文选择
    btn.setRequestFocusEnabled(false);
    btn.setMargin(JBUI.emptyInsets());
    btn.setPreferredSize(new Dimension(26, 26));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.addActionListener(e -> {
      if (!code.isBlank()) {
        CopyPasteManager.getInstance().setContents(new TextTransferable(code));
      }
      flash(btn);
    });
    return btn;
  }

  /**
   * 复制后短暂显示对勾，无 AllIcons.Actions.Checked 的版本可换 AllIcons.General.InspectionsOK
   */
  private static void flash(JButton btn) {
    Icon original = btn.getIcon();
    btn.setIcon(AllIcons.Actions.Checked);
    Timer t = new Timer(1200, ev -> {
      btn.setIcon(original);
      btn.repaint();
    });
    t.setRepeats(false);
    t.start();
  }

  // ---------- 代码块编辑器 ----------

  private static final class CodeField extends LanguageTextField {

    private final int fixedHeight;

    CodeField(Language language, Project project, String text) {
      super(language, project, text);
      setOneLineMode(false);
      setViewer(true);                 // 只读、无光标，但保留语法高亮
      setOpaque(false);
      setRequestFocusEnabled(false);
      setBorder(JBUI.Borders.empty(2, 8, 6, 8));

      addSettingsProvider(editor -> {
        EditorSettings s = editor.getSettings();
        s.setLineNumbersShown(false);
        s.setFoldingOutlineShown(false);
        s.setIndentGuidesShown(false);
        s.setUseSoftWraps(false);          // 代码不折行，走横向滚动
        s.setAdditionalLinesCount(0);
        s.setRightMarginShown(false);
        s.setCaretRowShown(false);
        s.setLineMarkerAreaShown(false);

        // 边框设在外层组件上，不再需要 EditorEx
        editor.getComponent().setBorder(JBUI.Borders.empty());
      });

      // 取当前 IDE 编辑器字体（等宽），不依赖任何废弃 API
      EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
      Font editorFont = new Font(scheme.getEditorFontName(), Font.PLAIN, scheme.getEditorFontSize());
      FontMetrics fm = getFontMetrics(editorFont);

      int lineHeight = fm.getHeight();
      int lines = Math.min(countLines(text), MAX_CODE_LINES);
      fixedHeight = lineHeight * Math.max(1, lines) + 14 + 8 + 4;

    }

    @Override
    public Dimension getPreferredSize() {
      int w = getParent() == null ? 0 : getParent().getWidth();
      return new Dimension(Math.max(0, w), fixedHeight);
    }

    @Override
    public Dimension getMaximumSize() {
      return new Dimension(Integer.MAX_VALUE, fixedHeight);
    }

    @Override
    public Dimension getMinimumSize() {
      return new Dimension(0, fixedHeight);
    }
  }

  // ---------- 正文文本区 ----------

  /**
   * 只读但仍可选中 + Ctrl+C；按可用宽度手动量高度，避免 BoxLayout 下宽度失控
   */
  private static final class WrappingTextArea extends JBTextArea {

    WrappingTextArea(String text) {
      super(text);
      setEditable(false);
      setLineWrap(true);
      setWrapStyleWord(true);
      setOpaque(true);
      setFont(JBFont.label());
      setForeground(UIUtil.getLabelForeground());
      setCaretColor(UIUtil.getLabelForeground());

      // 双击选中一段，方便手动复制
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) selectLineAt(e.getPoint());
        }
      });
    }

    private void selectLineAt(Point p) {
      int off = viewToModel(p);
      Element root = getDocument().getDefaultRootElement();
      int line = root.getElementIndex(off);
      select(root.getElement(line).getStartOffset(), root.getElement(line).getEndOffset());
    }

    @Override
    public Dimension getPreferredSize() {
      int w = getParent() == null ? 0 : getParent().getWidth();
      if (w <= 0) return super.getPreferredSize();

      Insets in = getInsets();
      FontMetrics fm = getFontMetrics(getFont());
      int innerW = Math.max(1, w - in.left - in.right);

      int lines = 0;
      for (String para : getText().split("\n", -1)) {
        if (para.isEmpty()) {
          lines++;
          continue;
        }
        lines += Math.max(1, (int) Math.ceil(fm.stringWidth(para) / (double) innerW));
      }
      return new Dimension(w, lines * fm.getHeight() + in.top + in.bottom);
    }

    @Override
    public Dimension getMaximumSize() {
      return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    public Dimension getMinimumSize() {
      return new Dimension(0, getPreferredSize().height);
    }
  }

  // ---------- 容器 / 边框 ----------

  /**
   * 宽度跟随视口、高度可滚动的 BoxLayout 容器
   */
  private static final class ScrollableBoxPanel extends JPanel implements Scrollable {
    ScrollableBoxPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
      return Math.max(16, r.height);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }

  private static final class RoundedBorder implements Border {
    private final Color color;
    private final int radius;

    RoundedBorder(Color color, int radius) {
      this.color = color;
      this.radius = radius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
      // 顺序：top, left, bottom, right
      return new Insets(4, 2, 2, 2);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.setStroke(new BasicStroke(1f));
      g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
      g2.dispose();
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }
  }

  // ---------- 工具 ----------

  private static Color codeBg() {
    EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
    return scheme.getDefaultBackground();
  }

  private static int countLines(String s) {
    if (s == null || s.isEmpty()) return 1;
    int n = 1;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '\n') n++;
    }
    return n;
  }

  /**
   * 围栏语言标签 -> IntelliJ Language，识别不了就退回纯文本
   */
  private static Language resolveLanguage(String tag) {
    if (tag.isBlank() || "code".equals(tag) || "text".equals(tag)) return PlainTextLanguage.INSTANCE;

    Map<String, String> alias = Map.ofEntries(
        Map.entry("js", "JavaScript"),
        Map.entry("jsx", "JavaScript"),
        Map.entry("ts", "TypeScript"),
        Map.entry("tsx", "TypeScript"),
        Map.entry("py", "Python"),
        Map.entry("sh", "Shell Script"),
        Map.entry("bash", "Shell Script"),
        Map.entry("zsh", "Shell Script"),
        Map.entry("yml", "YAML"),
        Map.entry("yaml", "YAML"),
        Map.entry("c++", "C++"),
        Map.entry("cpp", "C++"),
        Map.entry("cs", "C#"),
        Map.entry("csharp", "C#"),
        Map.entry("golang", "Go"),
        Map.entry("rs", "Rust"),
        Map.entry("objc", "ObjectiveC"),
        Map.entry("md", "Markdown"),
        Map.entry("kt", "Kotlin"),
        Map.entry("json", "JSON"),
        Map.entry("html", "HTML"),
        Map.entry("xml", "XML"),
        Map.entry("sql", "SQL"),
        Map.entry("java", "JAVA"),
        Map.entry("vue", "Vue"),
        Map.entry("css", "CSS"),
        Map.entry("scss", "Sass")
    );
    String target = alias.getOrDefault(tag.toLowerCase(Locale.ROOT), tag);

    Language byId = Language.findLanguageByID(target);
    if (byId != null) return byId;

    for (Language l : Language.getRegisteredLanguages()) {
      if (target.equalsIgnoreCase(l.getID())) return l;
      if (target.equalsIgnoreCase(l.getDisplayName())) return l;
    }
    return PlainTextLanguage.INSTANCE;
  }

  @Override
  public void dispose() {
    clearContent();
  }
}