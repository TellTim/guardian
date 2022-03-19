package io.telltim.common

/**
 * @author :Tim.WJ
 * Created on 2022/3/16.
 */


open class SingletonHolder1<out T : Any, in A>(creator: (A?) -> T) {

    private var creator: ((A?) -> T)? = creator

    @Volatile
    private var instance: T? = null

    /**
     * 获取单例
     * @param arg 带参单例，可为空
     */
    protected fun getInstanceInternal(arg: A? = null): T =
        instance ?: synchronized(this) {
            instance ?: creator!!(arg).apply {
                instance = this
            }
        }
}

open class SingletonHolder2<out T : Any, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    /**
     * 获取单例
     * @param arg 带参单例，不可为空
     */
    protected fun getInstanceInternal(arg: A): T =
        instance ?: synchronized(this) {
            instance ?: creator!!(arg).apply {
                instance = this
            }
        }
}


/**
 * If you need to pass only ONE argument to the constructor of the singleton class.
 * Make companion object extended from [SingletonUtil] for best match.
 * Ex:
class AppRepository private constructor(private val db: Database) {
companion object : SingleArgSingletonHolder<AppRepository, Database>(::AppRepository)
}
 * Uses:
val appRepository =  AppRepository.getInstance(db)
 */
open class SingletonUtil<out T : Any, in A>(creator: (A?) -> T) :
    SingletonHolder1<T, A>(creator) {
    fun getInstance(arg: A): T = getInstanceInternal(arg)
}

/**
 * If you need to pass TWO arguments to the constructor of the singleton class.
 * Extended from [SingletonWithPairArgsUtil] for best match.
 * Ex:
class AppRepository private constructor(private val db: Database, private val apiService: ApiService) {
companion object : PairArgsSingletonHolder<AppRepository, Database, ApiService>(::AppRepository)
}
 *
 * Uses:
val appRepository =  AppRepository.getInstance(db, apiService)
 */
open class SingletonWithPairArgsUtil<out T : Any, in A, in B>(creator: (A, B) -> T) :
    SingletonHolder2<T, Pair<A, B>>(creator = { (a, b) -> creator(a, b) }) {

    fun getInstance(arg1: A, arg2: B) = getInstanceInternal(Pair(arg1, arg2))
}

/**
 * If you need to pass THREE arguments to the constructor of the singleton class.
 * Extended from [TripleArgsSingletonHolder] for the best match.
 *
 * Ex:
class AppRepository private constructor(
private val db: Database,
private val apiService: ApiService,
private val storage : Storage
) {
companion object : TripleArgsSingletonHolder<AppRepository, Database, ApiService, Storage>(::AppRepository)
}
 *
 * Uses:
val appRepository =  AppRepository.getInstance(db, apiService, storage)
 */
open class TripleArgsSingletonHolder<out T : Any, in A, in B, in C>(creator: (A, B, C) -> T) :
    SingletonHolder2<T, Triple<A, B, C>>(creator = { (a, b, c) -> creator(a, b, c) }) {

    fun getInstance(arg1: A, arg2: B, arg3: C) = getInstanceInternal(Triple(arg1, arg2, arg3))
}