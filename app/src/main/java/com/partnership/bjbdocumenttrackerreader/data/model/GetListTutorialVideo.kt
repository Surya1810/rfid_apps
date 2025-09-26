package com.partnership.bjbdocumenttrackerreader.data.model

data class GetListTutorialVideo (
    val videos: List<TutorialVideo>
    )

data class TutorialVideo(
    val title: String,
    val link: String
)