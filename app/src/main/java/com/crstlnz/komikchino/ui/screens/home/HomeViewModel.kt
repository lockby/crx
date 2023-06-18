package com.crstlnz.komikchino.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ViewModelBase

class HomeViewModelFactory(
    private val storage: StorageHelper<HomeData>,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(storage) as T
    }
}

class HomeViewModel(storage: StorageHelper<HomeData>) :
    ViewModelBase<HomeData>(
        storage,
        false
    ) {

    private val api: Kiryuu = Kiryuu()
    override var cacheKey = "home"

    init {
        load(false)
    }

    override suspend fun fetchData(): HomeData {
        return api.getHome()
//        val body = api.getHome()
//        val document: Document = Jsoup.parse(body.string())
//        val featureds = document.select("#slidtop .bigslider .bigcover")
//
//        val result = arrayListOf<FeaturedComic>()
//        Log.d("FEATURED", featureds.html())
//        for (featured in featureds) {
//            val img = featured.selectFirst("img")?.attr("src") ?: ""
//            val type = featured.selectFirst(".seventyinfo .type")?.text() ?: ""
//            val url = featured.selectFirst("a")?.attr("href") ?: ""
//            val title = featured.selectFirst(".seventyinfo .ttseventy")?.text() ?: ""
//            val description =
//                featured.selectFirst(".seventyinfo.bottomseventy .ttseventy")?.text() ?: ""
//            val genre = featured.selectFirst(".seventyinfo.bottomseventy .extras")?.text() ?: ""
//            val slug = getLastPathSegment(featured.selectFirst("a")?.attr("href") ?: "") ?: ""
//            result.add(
//                FeaturedComic(
//                    title,
//                    url,
//                    description,
//                    genre,
//                    type,
//                    img,
//                    slug
//                )
//            )
//        }
//
//        return result


//        Log.d("FETCH", "MULAI")
//        val website_url =
//            "https://en.wikipedia.org/wiki/List_of_countries_and_dependencies_by_population"
//        return skrape(AsyncFetcher) {
//            request {
//                timeout = 10000
//                url = website_url
//            }
//
//            extractIt<ScrapingResult> { results ->
//                Log.d("EXTRACT", "MULAI")
//                htmlDocument {
//                    Log.d("PARSE", "MULAI")
//                    val countryRows = table(".wikitable") {
//                        tr {
//                            findAll { this }
//                        }
//                    }
//                    countryRows
//                        .drop(2)  // Remove the first two elements; these are just the table header and subheader
//                        .map {
//                            // Define variables to hold name and population
//                            var name: String = ""
//                            var population: String = ""
//                            it.a {
//                                findFirst() {   // Find the first <a> tag
//                                    name =
//                                        text    // Extract its text (this is the name of the country)
//                                    println("Name - $text ")
//                                }
//                            }
//                            it.td {
//                                findSecond() {    // Find the second <td> tag
//                                    population =
//                                        text   // Extract its text (this is the population of the country)
//                                    println("Population - $text \n")
//                                }
//                            }
//                            results.countries.add(
//                                Country(
//                                    name,
//                                    population
//                                )
//                            )   // Create a country and add it to the results object
//                            results.count =
//                                results.countries.size  // Get the number of countries and add it to the results object
//                        }
//                    // Main function where you'll parse web data
//                }
//            }
//        }

    }

}