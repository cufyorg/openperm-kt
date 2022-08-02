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
 * A permission is responsible for determining if
 * an entity have some kind of access to another
 * entity.
 *
 * @author LSafer
 * @since 1.0.0
 */
interface Permission<T> {
    /**
     * Evaluate the given permission.
     *
     * @param privilege the privilege
     * @param target the target to evaluate the permission for.
     * @return the approval objects.
     * @author LSafer
     * @since 1.0.0
     */
    suspend operator fun invoke(privilege: Privilege, target: T): List<Approval>

    companion object
}

/**
 * Return a permission that invokes the given [builder]
 * and return the result of invoking its results.
 *
 * @author LSafer
 * @since 1.0.0
 */
fun <T> Permission(
    builder: suspend Permission<T>.(Privilege, T) -> Permission<in T>
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return builder(privilege, target)(privilege, target)
    }
}

/**
 * Create a permission that returns a successful
 * approval when [value] is true and a failure
 * approval otherwise.
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun <T> Permission(
    value: Boolean,
    error: Throwable? = null
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return listOf(Approval(value, error))
    }
}

/**
 * Create a permission that always returns the given [approvals].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun <T> Permission(
    vararg approvals: Approval
) = Permission<T>(approvals.asList())

/**
 * Create a permission that always returns the given [approvals].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun <T> Permission(
    approvals: List<Approval>
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return approvals
    }
}

/**
 * Create a permission that returns the result of checking a permit from the given [roles].
 *
 * @since 1.1.0
 */
@JvmName("fromRole")
fun <T> Permission(
    vararg roles: Role
) = Permission<T>(roles.asList())

/**
 * Create a permission that returns the result of checking a permit from the given [roles].
 *
 * @since 1.1.0
 */
@JvmName("fromRole")
fun <T> Permission(
    roles: List<Role>
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return roles.flatMap { privilege(it) }
    }
}

/**
 * Create a permission that returns the result of checking the given [permits].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("fromPermit")
fun <T> Permission(
    vararg permits: Permit<in T>
) = Permission(permits.asList())

/**
 * Create a permission that returns the result of checking the given [permits].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("fromPermit")
fun <T> Permission(
    permits: List<Permit<in T>>
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return permits.flatMap { it(target).flatMap { privilege(it) } }
    }
}

/**
 * Return a permission that returns the approvals from invoking the given [permissions].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("combine")
fun <T> Permission(
    vararg permissions: Permission<in T>
) = Permission(permissions.asList())

/**
 * Return a permission that returns the approvals from invoking the given [permissions].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("combine")
fun <T> Permission(
    permissions: List<Permission<in T>>
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return permissions.flatMap { it(privilege, target) }
    }
}

// Util

/**
 * Create a new permission that returns the result of invoking the given
 * permission with the target being the result of invoking the given mapper
 * with the target given to it.
 *
 * @since 1.0.0
 */
fun <T, U> Permission.Companion.map(
    permission: Permission<U>,
    mapper: suspend (T) -> U
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return permission(privilege, mapper(target))
    }
}

// Implementation

/**
 * A permission that checks a list of permissions.
 *
 * If the permissions list is empty, the returned permission will always
 * evaluate to `true`.
 *
 * If at least one permission evaluates to false, the permission will
 * evaluate to `false`.
 *
 * If at least one permission doesn't emit an approval, the permission will
 * evaluate to `false`.
 *
 * @author LSafer
 * @since 1.0.0
 */
class EveryPermission<T>(private val permissions: List<Permission<in T>>) : Permission<T> {
    constructor(vararg permissions: Permission<in T>) : this(permissions.asList())

    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        if (permissions.isEmpty())
            return listOf(Approval(true, Denial.NoChecklist))

        var firstSuccess: Approval? = null

        for (permission in permissions) {
            val approvals = permission(privilege, target)

            if (approvals.isEmpty())
                return listOf(Approval(false, Denial.NoResults))

            for (approval in approvals)
                if (!approval.value)
                    return listOf(approval)

            firstSuccess = firstSuccess ?: approvals.firstOrNull()
        }

        return listOf(firstSuccess ?: Approval(true, Denial.NoResults))
    }
}

/**
 * Return a permission that checks a list of permissions.
 *
 * If the permissions list is empty, the returned permission will always
 * evaluate to `false`.
 *
 * If at least one permission evaluates to true, the permission will
 * evaluate to `true`.
 *
 * @author LSafer
 * @since 1.0.0
 */
class SomePermission<T>(private val permissions: List<Permission<in T>>) : Permission<T> {
    constructor(vararg permissions: Permission<in T>) : this(permissions.asList())

    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        if (permissions.isEmpty())
            return listOf(Approval(false, Denial.NoChecklist))

        var firstFailure: Approval? = null

        for (permission in permissions) {
            val approvals = permission(privilege, target)

            for (approval in approvals)
                if (approval.value)
                    return listOf(approval)

            firstFailure = firstFailure ?: approvals.firstOrNull()
        }

        return listOf(firstFailure ?: Approval(false, Denial.NoResults))
    }
}
