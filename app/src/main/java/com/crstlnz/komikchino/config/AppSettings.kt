package com.crstlnz.komikchino.config

import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.ui.navigations.HomeSections
import javax.inject.Singleton


@Singleton
object AppSettings {
    val homeDefaultRoute = HomeSections.HOME.route
    const val animationDuration = 180
    var komikServer: KomikServer? = null
}