package com.thinkfinitylabs.iotalarm

import android.net.Uri

interface MusicPickerListener {
    fun onMusicPick(uri: Uri, title: String)
    fun onPickCanceled()
}