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
 * An interface to standardize permissions.
 * In a simpler words:
 * A Standardize Permission Factory.
 *
 * Note: this interface is completely optional and
 * using this library without using this interface
 * will always be an option.
 *
 * @author LSafer
 * @since 1.2.0
 */
interface Permissions<T> {
    /**
     * Create a permission for the given arguments.
     *
     * @param permissions the parent permissions instance.
     * @since 1.3.0
     */
    operator fun invoke(
        access: Access,
        permissions: Permissions<T> = this
    ): Permission<T>

    companion object
}

/**
 * A builder that builds a [Permissions] object.
 *
 * @author LSafer
 * @since 1.3.0
 */
open class PermissionsBuilder<T> {
    /**
     * The factories to be used by the built
     * permissions instance.
     *
     * @since 1.3.0
     */
    val factories: MutableList<Permissions<T>.(Access) -> Permission<T>?> =
        mutableListOf()

    /**
     * Build the permissions instance from the
     * current state of this builder.
     *
     * @since 1.3.0
     */
    fun build(): Permissions<T> {
        val _factories = factories.toList()
        return object : Permissions<T> {
            override fun invoke(
                access: Access,
                permissions: Permissions<T>
            ) = SomePermission(Permission(
                _factories.mapNotNull {
                    it(permissions, access)
                }
            ))
        }
    }
}

// Builder

/**
 * Construct a new [Permissions] instance with the
 * given [block].
 *
 * @param block the builder block.
 * @since 1.3.0
 */
fun <T> Permissions(
    block: PermissionsBuilder<T>.() -> Unit = {}
): Permissions<T> {
    val builder = PermissionsBuilder<T>()
    builder.apply(block)
    return builder.build()
}

// Constructor

/**
 * Construct a new [Permissions] instance that
 * always returns a permission that always returns
 * no approvals.
 *
 * @since 1.3.0
 */
fun <T> Permissions() = object : Permissions<T> {
    override fun invoke(access: Access, permissions: Permissions<T>): Permission<T> {
        return Permission()
    }
}

/**
 * Return a [Permissions] instance that returns
 * the result of invoking the given [permissionses]
 * mapped using the given [mapper].
 *
 * @since 1.3.0
 */
fun <T, U> Permissions(
    permissions: Permissions<U>,
    vararg permissionses: Permissions<U>,
    mapper: suspend (T) -> U
) = Permissions(listOf(permissions, *permissionses), mapper)

/**
 * Return a [Permissions] instance that returns
 * the result of invoking the given [permissionses]
 * mapped using the given [mapper].
 *
 * @since 1.3.0
 */
fun <T, U> Permissions(
    permissionses: List<Permissions<U>>,
    mapper: suspend (T) -> U
) = object : Permissions<T> {
    override fun invoke(access: Access, permissions: Permissions<T>): Permission<T> {
        return Permission(permissionses.map { it(access) }, mapper)
    }
}

// Property

/**
 * Check for anonymous read ability for [T].
 *
 * @since 1.2.0
 */
val <T> Permissions<T>.AnonymousRead: Permission<T>
    get() = this(Access.AnonymousRead)

/**
 * Check for anonymous write ability for [T].
 *
 * @since 1.2.0
 */
val <T> Permissions<T>.AnonymousWrite: Permission<T>
    get() = this(Access.AnonymousWrite)

/**
 * Check for read ability for [T].
 *
 * @since 1.2.0
 */
val <T> Permissions<T>.Read: Permission<T>
    get() = this(Access.Read)

/**
 * Check for write ability for [T].
 *
 * @since 1.2.0
 */
val <T> Permissions<T>.Write: Permission<T>
    get() = this(Access.Write)

/**
 * Check for owner-level read ability for [T].
 *
 * @since 1.2.0
 */
val <T> Permissions<T>.OwnerRead: Permission<T>
    get() = this(Access.OwnerRead)

/**
 * Check for owner-level write ability for [T].
 *
 * @since 1.2.0
 */
val <T> Permissions<T>.OwnerWrite: Permission<T>
    get() = this(Access.OwnerWrite)

// Extension

/**
 * Add the given [block] to the factories list.
 *
 * @param block a permission factory.
 *              Invoked on factory invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.factory(
    block: Permissions<T>.(Access) -> Permission<T>?
) {
    this.factories += block
}

/**
 * Add a permission factory that returns a
 * permission that invokes the given [block].
 *
 * @param block the permission block.
 *              Invoked on permission invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.permission(
    block: suspend Permissions<T>.(Privilege, T) -> Permission<in T>
) {
    factory {
        Permission { privilege, target ->
            block(this@factory, privilege, target)
        }
    }
}

/**
 * Add the factories in the given [block] only if
 * [filter] returned `true`.
 *
 * @param filter a configuration predicate.
 *              Invoked on factory invocation.
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.filter(
    filter: (Access) -> Boolean,
    block: PermissionsBuilder<T>.() -> Unit
) {
    val builder = PermissionsBuilder<T>()
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

/**
 * Add a factory that combines the result of the
 * factories in the given [block] using the given
 * [combinator].
 *
 * @param combinator a permissions combining function.
 *              Invoked on factory invocation.
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.combine(
    combinator: (List<Permission<T>>) -> Permission<T> = ::Permission,
    block: PermissionsBuilder<T>.() -> Unit
) {
    val builder = PermissionsBuilder<T>()
    builder.apply(block)
    val _factories = builder.factories.toList()
    factory { access ->
        combinator(
            _factories.mapNotNull {
                it(this, access)
            }
        )
    }
}

//

/**
 * Add a factory that returns the result of
 * invoking the permissions instance returned from
 * the given [block].
 *
 * @param block a permissions instance supplier.
 *              Invoked right away.
 * @since 1.3.0
 */
@JvmName("appendPermissions")
@OverloadResolutionByLambdaReturnType
fun <T> PermissionsBuilder<T>.append(
    block: () -> Permissions<T>
) {
    val permissions = block()

    factory { access ->
        permissions(access, this)
    }
}

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
fun <T> PermissionsBuilder<T>.append(
    block: () -> Permits<in T>
) {
    val permits = block()

    factory { access ->
        Permission(permits(access))
    }
}

/**
 * Add a factory that returns the result of
 * invoking the given [block].
 *
 * @param block a permission supplier.
 *              Invoked right away.
 * @since 1.3.0
 */
@JvmName("appendPermission")
@OverloadResolutionByLambdaReturnType
fun <T> PermissionsBuilder<T>.append(
    block: () -> Permission<in T>
) {
    val permission = block()

    factory { Permission(permission) }
}

//

/**
 * Add a factory that combines the result of the
 * factories in the given [block] using
 * [SomePermission].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.some(
    block: PermissionsBuilder<T>.() -> Unit
) {
    combine(::SomePermission, block)
}

/**
 * Add a factory that combines the result of the
 * factories in the given [block] using
 * [EveryPermission].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.every(
    block: PermissionsBuilder<T>.() -> Unit
) {
    combine(::EveryPermission, block)
}

//

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Operation.Read].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.readOperation(
    block: PermissionsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Operation.Read }, block)
}

/**
 * Add a permission factory that only works when
 * the access is [Access.Operation.Read] and
 * returns a permission that invokes the given
 * [block].
 *
 * @param block the permission block.
 *              Invoked on permission invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.read(
    block: suspend Permissions<T>.(Privilege, T) -> Permission<in T>
) {
    readOperation { permission(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Operation.Write].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.writeOperation(
    block: PermissionsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Operation.Write }, block)
}

/**
 * Add a permission factory that only works when
 * the access is [Access.Operation.Write] and
 * returns a permission that invokes the given
 * [block].
 *
 * @param block the permission block.
 *              Invoked on permission invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.write(
    block: suspend Permissions<T>.(Privilege, T) -> Permission<in T>
) {
    writeOperation { permission(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Level.Anonymous].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.anonymousLevel(
    block: PermissionsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Level.Anonymous }, block)
}

/**
 * Add a permission factory that only works when
 * the access is [Access.Level.Anonymous] and
 * returns a permission that invokes the given
 * [block].
 *
 * @param block the permission block.
 *              Invoked on permission invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.anonymous(
    block: suspend Permissions<T>.(Privilege, T) -> Permission<in T>
) {
    anonymousLevel { permission(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Level.Default].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.defaultLevel(
    block: PermissionsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Level.Default }, block)
}

/**
 * Add a permission factory that only works when
 * the access is [Access.Level.Default] and
 * returns a permission that invokes the given
 * [block].
 *
 * @param block the permission block.
 *              Invoked on permission invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.default(
    block: suspend Permissions<T>.(Privilege, T) -> Permission<in T>
) {
    defaultLevel { permission(block) }
}

/**
 * Add the factories in the given [block] only if
 * the access is [Access.Level.Owner].
 *
 * @param block a permission builder config block.
 *              Invoked right away.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.ownerLevel(
    block: PermissionsBuilder<T>.() -> Unit
) {
    filter({ it is Access.Level.Owner }, block)
}

/**
 * Add a permission factory that only works when
 * the access is [Access.Level.Owner] and
 * returns a permission that invokes the given
 * [block].
 *
 * @param block the permission block.
 *              Invoked on permission invocation.
 * @since 1.3.0
 */
fun <T> PermissionsBuilder<T>.owner(
    block: suspend Permissions<T>.(Privilege, T) -> Permission<in T>
) {
    ownerLevel { permission(block) }
}
