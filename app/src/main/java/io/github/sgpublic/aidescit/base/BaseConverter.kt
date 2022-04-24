package io.github.sgpublic.aidescit.base

interface BaseConverter<T, V> {
    fun encode(obj: T): V
    fun decode(value: V): T
}