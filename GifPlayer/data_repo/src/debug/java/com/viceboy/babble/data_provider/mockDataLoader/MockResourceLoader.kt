package com.viceboy.babble.data_provider.mockDataLoader

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.viceboy.data_repo.model.dataModel.User
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Serializable

class MockResourceLoader private constructor() {

    companion object {
        private val TAG = "mock_resource"
        private val gson = GsonBuilder().create()

        @JvmStatic
        fun getResponseFrom(
            context: Context,
            method: String,
            endPoints: Array<String>
        ): List<User>? {
            try {
                var currentPath = "mock"
                var mockLists = context.assets.list(currentPath)

                for (endpoint in endPoints) {
                    if (mockLists!!.contains(endpoint)) {
                        currentPath = "$currentPath/$endpoint"
                        mockLists = context.assets.list(currentPath)
                    }
                }

                var finalPath: String? = null
                for (path in mockLists!!) {
                    if (path.contains(method)) {
                        finalPath = "$currentPath/$path"
                        break
                    }
                }
                if (!finalPath.isNullOrEmpty()) {
                    val descriptor = context.assets.open(finalPath)
                    val reader = BufferedReader(InputStreamReader(descriptor))
                    val sb = reader.use {
                        it.readText()
                    }
                    val type = object : TypeToken<List<User>>() {}.type
                    return gson.fromJson<List<User>>(sb, type)
                }
                return null
            } catch (e: IOException) {
                Log.d(TAG, "Error loading mock response from assets")
                return null
            }
        }

        @JvmStatic
        fun getDummyResponseFrom(
            context: Context,
            method: String,
            endPoints: Array<String>
        ): HashMap<String, Any>? {
            try {
                var currentPath = "mock"
                var mockLists = context.assets.list(currentPath)

                for (endpoint in endPoints) {
                    if (mockLists!!.contains(endpoint)) {
                        currentPath = "$currentPath/$endpoint"
                        mockLists = context.assets.list(currentPath)
                    }
                }

                var finalPath: String? = null
                for (path in mockLists!!) {
                    if (path.contains(method)) {
                        finalPath = "$currentPath/$path"
                        break
                    }
                }
                if (!finalPath.isNullOrEmpty()) {
                    val descriptor = context.assets.open(finalPath)
                    val reader = BufferedReader(InputStreamReader(descriptor))
                    val sb = reader.use {
                        it.readText()
                    }

                    return gson.fromJson(sb, HashMap<String, Any>()::class.java)
                }
                return null
            } catch (e: IOException) {
                Log.d(TAG, "Error loading mock response from assets")
                return null
            }
        }

        inline fun <reified T : Serializable> getObjectBeanFromMap(map: LinkedTreeMap<String, Any>): T {
            val builder = GsonBuilder()
            val gson = builder.create()
            val mapToString = gson.toJson(map)
            return gson.fromJson(mapToString, T::class.java)
        }

        inline fun <reified T : Serializable> mockApiData(
            searchKey: String,
            cachedMap: HashMap<String, T>,
            inMap: LinkedTreeMap<String, Any>
        ): Maybe<T> {
            return Maybe.create { emitter ->
                val apiData = inMap.entries.firstOrNull {
                    it.key == searchKey
                }?.value?.toObject<T>()
                apiData?.let {
                    cachedMap[searchKey] = it
                    emitter.onSuccess(it)
                    return@create
                }
                emitter.onComplete()
            }
        }

        inline fun <reified T : Serializable> cachedData(
            searchKey: String,
            cachedMap: HashMap<String, T>
        ): Maybe<T> {
            return Maybe.create { emitter ->
                val cachedVal = cachedMap.entries.firstOrNull {
                    it.key == searchKey
                }?.value
                cachedVal?.let {
                    emitter.onSuccess(it)
                    return@create
                }
                emitter.onComplete()
            }
        }

        inline fun <reified T : Serializable> getModelData(
            searchKey: String,
            cachedMap: HashMap<String, T>,
            inputMap: LinkedTreeMap<String, Any>
        ): Flowable<T> {
            return Maybe.concat(
                cachedData(searchKey, cachedMap),
                mockApiData(searchKey, cachedMap, inputMap)
            )
        }

        inline fun <reified T : Serializable> getModelData(
            searchKey: Array<String>,
            inputMap: LinkedTreeMap<String, Any>
        ): Flowable<List<T>> {
            return Flowable.create({ emitter ->
                val apiData = inputMap.entries
                    .filter {searchKey.contains(it.key)}
                    .map { it.value.toObject<T>() }
                if (apiData.isNotEmpty())
                    emitter.onNext(apiData)
                else
                    emitter.onComplete()
            }, BackpressureStrategy.LATEST)

        }

        inline fun <reified T : Serializable> queryDataListWithParams(
            mapOfModel: LinkedTreeMap<String, Any>?,
            queryString: String,
            searchParams: String
        ): List<T> {
            val listOfModel = mutableListOf<T>()
            mapOfModel?.let { map ->
                for (id in map) {
                    val model = id.value as LinkedTreeMap<*, *>
                    val queryValue = model[queryString].toString()
                    if (queryValue == searchParams) listOfModel.add(model.toObject())
                }
            }
            return listOfModel
        }

        inline fun <reified T : Serializable> queryDataWithParams(
            mapOfModel: LinkedTreeMap<String, Any>?,
            queryString: String,
            searchParams: String
        ): T? {
            mapOfModel?.let { map ->
                for (id in map) {
                    val model = id.value as LinkedTreeMap<*, *>
                    val queryValue = model[queryString].toString()
                    if (queryValue == searchParams) return model.toObject()
                }
            }
            return null
        }

    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Serializable> Any.toObject(): T {
    return MockResourceLoader.getObjectBeanFromMap(this as LinkedTreeMap<String, Any>)
}