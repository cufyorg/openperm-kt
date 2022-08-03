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
 * Create a permit that returns no roles.
 *
 * @since 1.3.0
 */
fun <T> Permit() = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return emptyList()
    }
}

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
 * Create a permit that returns the result of invoking the given [permits] with
 * the target being the result of invoking the given [mapper] with the target given to it.
 *
 * @since 1.0.0
 */
@JvmName("map")
fun <T, U> Permit(
    permit: Permit<U>,
    vararg permits: Permit<U>,
    mapper: suspend (T) -> U
) = Permit(listOf(permit, *permits), mapper)

/**
 * Create a permit that returns the result of invoking the given [permits] with
 * the target being the result of invoking the given [mapper] with the target given to it.
 *
 * @since 1.0.0
 */
@JvmName("map")
fun <T, U> Permit(
    permits: List<Permit<U>>,
    mapper: suspend (T) -> U
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return permits.flatMap { it(mapper(target)) }
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

// Extension

/**
 * Check the given permit and throw the error if it fails.
 *
 * @param permit the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @author LSafer
 * @since 1.3.0
 */
@JvmName("requirePermit")
suspend fun <T> require(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = permit.require(privilege, target)

/**
 * Check this permit and throw the error if it fails.
 *
 * @receiver the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @author LSafer
 * @since 1.3.0
 */
suspend fun <T> Permit<T>.require(
    privilege: Privilege,
    target: T
): T {
    val approval = check(privilege, target)

    if (!approval.value)
        throw approval.error ?: Denial.Unspecified

    return target
}

/**
 * Check the given permit.
 *
 * @param permit the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return true, if the privilege is permitted the given permit for the given target.
 * @author LSafer
 * @since 1.3.0
 */
@JvmName("testPermit")
suspend fun <T> test(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = permit.test(privilege, target)

/**
 * Check this permit.
 *
 * @receiver the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return true, if the privilege is permitted the given permit for the given target.
 * @author LSafer
 * @since 1.3.0
 */
suspend fun <T> Permit<T>.test(
    privilege: Privilege,
    target: T
): Boolean {
    val approval = check(privilege, target)

    return approval.value
}

/**
 * Check the given permit.
 *
 * @param permit the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return an approval object.
 * @author LSafer
 * @since 1.3.0
 */
@JvmName("checkPermit")
suspend fun <T> check(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = permit.check(privilege, target)

/**
 * Check this permit.
 *
 * @receiver the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return an approval object.
 * @author LSafer
 * @since 1.3.0
 */
suspend fun <T> Permit<T>.check(
    privilege: Privilege,
    target: T
): Approval {
    val roles = this(target)

    if (roles.isEmpty())
        return Approval(false, Denial.NoChecklist)

    val successes = mutableListOf<Approval>()

    for (role in roles) {
        val approvals = privilege(role)

        if (approvals.isEmpty()) {
            return Approval(false, role.error, successes)
        }

        val failure = approvals.indexOfFirst { !it.value }

        if (failure >= 0) {
            return approvals[failure].suppress(
                successes + approvals.filterIndexed { i, _ -> i != failure }
            )
        }

        successes += approvals
    }

    if (successes.isEmpty()) {
        return Approval(true, roles[0].error)
    }

    return successes[0].suppress(successes.drop(1))
}
