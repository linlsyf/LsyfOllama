package com.lsyf.lsyfollama.vo;

import lombok.Data;

@Data
public class DiffMsg {
    String gitmsg;
    String result;

    public String getGitmsg() {
        return gitmsg;
    }

    public void setGitmsg(String gitmsg) {
        this.gitmsg = gitmsg;
    }

//    public String getResult() {
//        return result;
//    }

    public void setResult(String result) {
        this.result = result;
    }
}
