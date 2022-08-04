/*
 *	Copyright 2022 cufy.org
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */
@file:OptIn(ExperimentalTypeInference::class)

package org.cufy.openperm

import kotlin.experimental.ExperimentalTypeInference

/**
 * An interface to standardize permits.
 *
 * Note: this interface is completely optional and
 * using this library without using this interface
 * will always be an option.
 *
 * @author LSafer
 * @since 1.2.0
 */
interface Permits<T> {
    /**
     * Create a permit for the given arguments.
     *
     * @param permits the parent permits instance.
     * @since 1.3.0
     */
    operator fun invoke(
        access: Access,
        permits: Permits<T> = this
    ): Permit<T>

    companion object
}

/**
 * A builder that builds a [Permits] object.
 *
 * @author LSafer
 * @since 1.3.0
 */
open class PermitsBuilder<T> {
    /**
     * The factories to be used by the built
     * permits instance.
     *
     * @since 1.3.0
     */
    val factories: MutableList<Permits<T>.(Access) -> Permit<T>?> =
        mutableListOf()

    /**
     * Build the permits instance from the
     * current state of this builder.
     *
     * @since 1.3.0
     */
    fun build(): Permits<T> {
        val _factories = factories.toList()
        return object : Permits<T> {
            override fun invoke(
                access: Access,
                permits: Permits<T>
            ) = Permit(
                _factories.mapNotNull {
                    it(permits, access)
                }
            )
        }
    }
}

// Builder

/**
 * Construct a new [Permits] instance with the
 * given [block].
 *
 * @param block the builder block.
 * @since 1.3.0
 */
fun <T> Permits(
    block: PermitsBuilder<T>.() -> Unit = {}
): Permits<T> {
    val builder = PermitsBuilder<T>()
    builder.apply(block)
    return builder.build()
}

// Constructor

/**
 * Construct a new [Permits] instance that always
 * returns a permit that always returns no roles.
 *
 * @since 1.3.0
 */
fun <T> Permits() = object : Permits<T> {
    override fun invoke(access: Access, permits: Permits<T>): Permit<T> {
        return Permit()
    }
}

/**
 * Return a [Permits] instance that returns
 * the result of invoking the given [permitses]
 * mapped using the given [mapper].
 *
 * @since 1.3.0
 */
fun <T, U> Permits(
    permitses: List<Permits<U>>,
    mapper: suspend (T) -> U
) = object : Permits<T> {
    override fun invoke(access: Access, permits: Permits<T>): Permit<T> {
        return Permit(permitses.map { it(access) }, mapper)
    }
}

// Property

/**
 * Check for a written permit for owning [T].
 *
 * @since 1.2.0
 */
val <T> Permits<T>.Owner: Permit<T>
    get() = this(Access.Level.Owner)

/**
 * Check for a written permit for anonymously access [T].
 *
 * @since 1.3.0
 */
val <T> Permits<T>.Anonymous: Permit<T>
    get() = this(Access.Level.Anonymous)

/**
 * Check for a written permit for reading [T].
 *
 * @since 1.2.0
 */
val <T> Permits<T>.Read: Permit<T>
    get() = this(Access.Operation.Read)

/**
 * Check for a written permit for writing [T].
 *
 * @since 1.2.0
 */
val <T> Permits<T>.Write: Permit<T>
    get() = this(Access.Operation.Write)

// Extension

/**
 * Add the given [block] to the factories list.
 *
 * @param block a permit factory.
 *              Invoked on factory invocation.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.factory(
    block: Permits<T>.(Access) -> Permit<T>?
) {
    this.factories += block
}

/**
 * Add a permit factory that returns a
 * permit that invokes the given [block].
 *
 * @param block the permit block.
 *              Invoked on permit invocation.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.permit(
    block: suspend Permits<T>.(T) -> Permit<in T>
) {
    factory {
        Permit { target ->
            block(this@factory, target)
        }
    }
}

/**
 * Add the factories in the given [block] only if
 * [filter] returned `true`.
 *
 * @param filter a configuration predicate.
 *              Invoked on factory invocation.
 * @param block a permit builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.filter(
    filter: (Access) -> Boolean,
    block: PermitsBuilder<T>.() -> Unit
) {
    val builder = PermitsBuilder<T>()
    builder.apply(block)
    builder.factories.forEach { factory ->
        factory { access ->
            when {
                filter(access) -> factory(access)
                else -> null
            }
        }
    }
}

//

/**
 * Add a factory that returns the result of
 * invoking the permits instance returned from
 * the given [block].
 *
 * @param block a permits instance supplier.
 *              Invoked right away.
 * @since 1.3.0
 */
@JvmName("appendPermits")
@OverloadResolutionByLambdaReturnType
fun <T> PermitsBuilder<T>.append(
    block: () -> Permits<T>
) {
    val permits = block()

    factory { access ->
        permits(access, this)
    }
}

/**
 * Add a factory that returns the result of
 * invoking the given [block].
 *
 * @param block a permit supplier.
 *              Invoked right away.
 * @since 1.3.0
 */
@JvmName("appendPermit")
@OverloadResolutionByLambdaReturnType
fun <T> PermitsBuilder<T>.append(
    block: () -> Permit<in T>
) {
    val permit = block()

    factory { Permit(permit) }
}

//

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Operation.Read].
 *
 * @param block a permit builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.readOperation(
    block: PermitsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Operation.Read }, block)
}

/**
 * Add a permit factory that only works when
 * the access is [Access.Operation.Read] and
 * returns a permit that invokes the given
 * [block].
 *
 * @param block the permit block.
 *              Invoked on permit invocation.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.read(
    block: suspend Permits<T>.(T) -> Permit<in T>
) {
    readOperation { permit(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Operation.Write].
 *
 * @param block a permit builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.writeOperation(
    block: PermitsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Operation.Write }, block)
}

/**
 * Add a permit factory that only works when
 * the access is [Access.Operation.Write] and
 * returns a permit that invokes the given
 * [block].
 *
 * @param block the permit block.
 *              Invoked on permit invocation.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.write(
    block: suspend Permits<T>.(T) -> Permit<in T>
) {
    writeOperation { permit(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Level.Anonymous].
 *
 * @param block a permit builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.anonymousLevel(
    block: PermitsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Level.Anonymous }, block)
}

/**
 * Add a permit factory that only works when
 * the access is [Access.Level.Anonymous] and
 * returns a permit that invokes the given
 * [block].
 *
 * @param block the permit block.
 *              Invoked on permit invocation.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.anonymous(
    block: suspend Permits<T>.(T) -> Permit<in T>
) {
    anonymousLevel { permit(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Level.Owner].
 *
 * @param block a permit builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.ownerLevel(
    block: PermitsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Level.Owner }, block)
}

/**
 * Add a permit factory that only works when
 * the access is [Access.Level.Owner] and
 * returns a permit that invokes the given
 * [block].
 *
 * @param block the permit block.
 *              Invoked on permit invocation.
 * @since 1.3.0
 */
fun <T> PermitsBuilder<T>.owner(
    block: suspend Permits<T>.(T) -> Permit<in T>
) {
    ownerLevel { permit(block) }
}
