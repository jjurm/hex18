package com.treecio.hexplore.utils

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.IOException
import java.net.URL

class ImageUtil {

    fun bitmapFromUrl(url:String): Bitmap? {
        var image:Bitmap? = null
        try {
            val url = URL(url)
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream())

        } catch (e: IOException) {
            System.out.println(e)
        }
        return image
    }
}