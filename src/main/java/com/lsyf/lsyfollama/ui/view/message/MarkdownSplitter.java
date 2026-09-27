package com.lsyf.lsyfollama.ui.view.message;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 markdown 切成「正文段 / 代码块段」。
 *
 * <p>容错点：
 * <ul>
 *   <li>围栏支持 ``` 、''' 、~~~ 三种写法（有些模型会输出单引号）</li>
 *   <li>支持 CRLF / CR / LF</li>
 *   <li>围栏可缩进、可带语言信息串（```java linenums 取第一个 token）</li>
 *   <li>流式未闭合的围栏也会兜底成代码块，不会丢内容</li>
 * </ul>
 */
public final class MarkdownSplitter {

  private MarkdownSplitter() {
  }

  public static List<MdSegment> parse(String markdown) {
    List<MdSegment> out = new ArrayList<>();
    if (markdown == null || markdown.isEmpty()) return out;

    StringBuilder text = new StringBuilder();
    StringBuilder code = new StringBuilder();
    String lang = "";
    String openFence = null;
    boolean inCode = false;

    for (String line : markdown.split("\\R", -1)) {
      String fence = fenceOf(line);

      if (fence != null && !inCode) {
        if (!text.isEmpty()) {
          out.add(MdSegment.text(trimTail(text)));
          text.setLength(0);
        }
        String info = line.trim().substring(fence.length()).trim();
        int sp = info.indexOf(' ');
        lang = sp < 0 ? info : info.substring(0, sp);
        openFence = fence;
        inCode = true;
      } else if (fence != null && inCode && matchesClose(fence, openFence)) {
        out.add(MdSegment.code(lang, trimTail(code)));
        code.setLength(0);
        lang = "";
        openFence = null;
        inCode = false;
      } else if (inCode) {
        code.append(line).append('\n');
      } else {
        text.append(line).append('\n');
      }
    }

    // 流式中围栏可能还没闭合，兜底成代码块
    if (inCode) out.add(MdSegment.code(lang, trimTail(code)));
    else if (!text.isEmpty()) out.add(MdSegment.text(trimTail(text)));

    return out;
  }

  /**
   * 返回该行的围栏串（``` / ''' / ~~~），不是围栏行返回 null。
   * 语言信息串里不能出现同类符号，避免误判。
   */
  private static String fenceOf(String line) {
    String t = line.trim();
    if (t.length() < 3) return null;

    char c = t.charAt(0);
    if (c != '`' && c != '\'' && c != '~') return null;

    int i = 0;
    while (i < t.length() && t.charAt(i) == c) i++;
    if (i < 3) return null;

    String rest = t.substring(i);
    if (rest.indexOf('`') >= 0 || rest.indexOf('\'') >= 0 || rest.indexOf('~') >= 0) return null;

    return t.substring(0, i);
  }

  /** 闭合围栏必须与开始围栏同字符、且长度不小于开始围栏 */
  private static boolean matchesClose(String fence, String openFence) {
    if (openFence == null || fence.isEmpty() || openFence.isEmpty()) return false;
    return fence.charAt(0) == openFence.charAt(0) && fence.length() >= openFence.length();
  }

  private static String trimTail(StringBuilder sb) {
    int end = sb.length();
    while (end > 0 && (sb.charAt(end - 1) == '\n' || sb.charAt(end - 1) == '\r')) end--;
    return sb.substring(0, end);
  }
}
