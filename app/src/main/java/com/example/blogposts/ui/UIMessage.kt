package com.example.blogposts.ui

data class UIMessage(
    val message: String,
    val uiMessage: UIMessageType
)

sealed class UIMessageType {

    class Toast : UIMessageType()

    class Dialog : UIMessageType()

    class AreYouSureDialog(
        val callback: AreYouSureCallback
    ) : UIMessageType()

    class None : UIMessageType()
}