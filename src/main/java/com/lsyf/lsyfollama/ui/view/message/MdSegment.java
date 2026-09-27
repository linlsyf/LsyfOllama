package com.lsyf.lsyfollama.ui.view.message;


import java.util.Locale;

public record MdSegment(boolean code, String lang, String body) {

    public static MdSegment text(String body) {
        return new MdSegment(false, "", body);
    }

    public static MdSegment code(String lang, String body) {
        return new MdSegment(true, lang == null ? "" : lang, body);
    }

    /** 展示用的语言名，无语言时显示 code */
    public String displayLang() {
        return lang == null || lang.isBlank() ? "code" : lang.trim().toLowerCase(Locale.ROOT);
    }
}