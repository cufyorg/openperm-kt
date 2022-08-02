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
package org.cufy.openperm

/**
 * A permit is responsible for generating roles for a given target.
 *
 * @author LSafer
 * @since 1.0.0
 */
interface Permit<T> {
    /**
     * Evaluate the permit
     *
     * @param target the target to evaluate the permit for.
     * @return the roles to test when checking the permit.
     * @author LSafer
     * @since 1.0.0
     */
    suspend operator fun invoke(target: T): List<Role>

    companion object
}

// Builder

/**
 * Return a permit that invokes the given [builder]
 * and return the result of invoking its results.
 *
 * @author LSafer
 * @since 1.0.0
 */
fun <T> Permit(
    builder: suspend Permit<T>.(T) -> Permit<in T>
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return builder(target)(target)
    }
}

// Constructor

/**
 * Create a permit that returns a role with the
 * given [error].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun <T> Permit(
    error: Throwable? = null
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return listOf(Role(error))
    }
}

/**
 * Return a permit that returns the given [roles].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun <T> Permit(
    vararg roles: Role
) = Permit<T>(roles.asList())

/**
 * Return a permit that returns the given [roles].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun <T> Permit(
    roles: List<Role>
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return roles
    }
}

/**
 * Return a permit that returns the roles from invoking the given [permits].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("combine")
fun <T> Permit(
    vararg permits: Permit<in T>
) = Permit(permits.asList())

/**
 * Return a permit that returns the roles from invoking the given [permits].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("combine")
fun <T> Permit(
    permits: List<Permit<in T>>
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return permits.flatMap { it(target) }
    }
}

// Util

/**
 * Create a permit that returns the result of invoking the given permit with
 * the target being the result of invoking the given mapper with the target given to it.
 *
 * @since 1.0.0
 */
fun <T, U> Permit.Companion.map(
    permit: Permit<U>,
    mapper: suspend (T) -> U
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return permit(mapper(target))
    }
}
