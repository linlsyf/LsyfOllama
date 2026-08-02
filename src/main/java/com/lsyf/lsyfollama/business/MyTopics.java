package com.lsyf.lsyfollama.business;

import com.intellij.util.messages.Topic;

public  class MyTopics {


  public static final Topic<MyBroadcastListener> MY_BROADCAST_TOPIC =
      Topic.create("MyPlugin.Broadcast", MyBroadcastListener.class);
}
