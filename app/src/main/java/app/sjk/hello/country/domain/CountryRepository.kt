package app.sjk.hello.country.domain

interface CountryRepository {
    fun getCountryMap() : Map<String, Country>
}