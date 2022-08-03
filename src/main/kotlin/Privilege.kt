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
 * A privilege is responsible for validating if a
 * role is granted or not.
 *
 * @author LSafer
 * @since 1.0.0
 */
interface Privilege {
    /**
     * Evaluate the privilege.
     *
     * @param role the role to evaluate the privilege with.
     * @return the approval objects.
     * @since 1.0.0
     */
    suspend operator fun invoke(role: Role): List<Approval>

    companion object
}

// Builder

/**
 * Return a privilege that invokes the given [builder]
 * and return the result of invoking its results.
 *
 * @author LSafer
 * @since 1.0.0
 */
fun Privilege(
    builder: suspend Privilege.(Role) -> Privilege
) = object : Privilege {
    override suspend fun invoke(role: Role): List<Approval> {
        return builder(role)(role)
    }
}

// Constructor

/**
 * Create a privilege that returns no approvals.
 *
 * @since 1.3.0
 */
fun Privilege() = object : Privilege {
    override suspend fun invoke(role: Role): List<Approval> {
        return emptyList()
    }
}

/**
 * Create a privilege that returns a successful
 * approval when [value] is true and a failure
 * approval otherwise.
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun Privilege(
    value: Boolean,
    error: Throwable? = null
) = object : Privilege {
    override suspend fun invoke(role: Role): List<Approval> {
        return listOf(Approval(value, error ?: role.error))
    }
}

/**
 * Create a privilege that always returns the given [approvals].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun Privilege(
    vararg approvals: Approval
) = Privilege(approvals.asList())

/**
 * Create a privilege that always returns the given [approvals].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("result")
fun Privilege(
    approvals: List<Approval>
) = object : Privilege {
    override suspend fun invoke(role: Role): List<Approval> {
        return approvals
    }
}

/**
 * Return a privilege that returns the approvals from invoking the given [privileges].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("combine")
fun Privilege(
    vararg privileges: Privilege
) = Privilege(privileges.asList())

/**
 * Return a privilege that returns the approvals from invoking the given [privileges].
 *
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("combine")
fun Privilege(
    privileges: List<Privilege>
) = object : Privilege {
    override suspend fun invoke(role: Role): List<Approval> {
        return privileges.flatMap { it(role) }
    }
}

// Extension

/**
 * Check the given privilege and throw the error if it fails.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return the role.
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("requirePrivilege")
suspend fun require(
    privilege: Privilege,
    role: Role
) = privilege.require(role)

/**
 * Check this privilege and throw the error if it fails.
 *
 * @receiver the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return the role.
 * @author LSafer
 * @since 1.3.0
 */
suspend fun Privilege.require(
    role: Role
): Role {
    val approval = check(role)

    if (!approval.value)
        throw approval.error ?: Denial.Unspecified

    return role
}

/**
 * Check the given privilege.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return true, if the privilege has approval for the given role.
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("testPrivilege")
suspend fun test(
    privilege: Privilege,
    role: Role
) = privilege.test(role)

/**
 * Check this privilege.
 *
 * @receiver the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return true, if the privilege has approval for the given role.
 * @author LSafer
 * @since 1.3.0
 */
suspend fun Privilege.test(
    role: Role
): Boolean {
    val approval = check(role)

    return approval.value
}

/**
 * Check the given privilege.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return an approval object.
 * @author LSafer
 * @since 1.0.0
 */
@JvmName("checkPrivilege")
suspend fun check(
    privilege: Privilege,
    role: Role
) = privilege.check(role)

/**
 * Check this privilege.
 *
 * @receiver the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return an approval object.
 * @author LSafer
 * @since 1.3.0
 */
suspend fun Privilege.check(
    role: Role
): Approval {
    val approvals = this(role)

    if (approvals.isEmpty())
        return Approval(false, role.error)

    for (approval in approvals)
        if (!approval.value)
            return approval

    return approvals[0]
}

// Implementation

/**
 * A privilege that caches resolved roles.
 *
 * @author LSafer
 * @since 1.0.0
 */
class CachedPrivilege(private val privilege: Privilege) : Privilege {
    val cache: MutableMap<Role, List<Approval>> = mutableMapOf()

    override suspend fun invoke(role: Role): List<Approval> {
        return cache.getOrPut(role) { privilege(role) }
    }
}

/**
 * A privilege that checks a list of privileges.
 *
 * If the privileges list is empty, the privilege will always
 * evaluate to `false`.
 *
 * If at least one privilege evaluates to true, the privilege will
 * evaluate to `true`.
 *
 * @author LSafer
 * @since 1.0.0
 */
class SomePrivilege(private val privileges: List<Privilege>) : Privilege {
    constructor(vararg privileges: Privilege) : this(privileges.asList())

    override suspend fun invoke(role: Role): List<Approval> {
        if (privileges.isEmpty())
            return listOf(Approval(false, role.error))

        var firstFailure: Approval? = null

        for (privilege in privileges) {
            val approvals = privilege(role)

            for (approval in approvals)
                if (approval.value)
                    return listOf(approval)

            firstFailure = firstFailure ?: approvals.firstOrNull()
        }

        return listOf(firstFailure ?: Approval(false, role.error))
    }
}

/**
 * A privilege that checks a list of privileges.
 *
 * If the privileges list is empty, the returned privilege will always
 * evaluate to `true`.
 *
 * If at least one privilege evaluates to false, the privilege will
 * evaluate to `false`.
 *
 * If at least on privilege doesn't emit an approval, the privilege will
 * evaluate to `false`.
 *
 * @author LSafer
 * @since 1.0.0
 */
class EveryPrivilege(private val privileges: List<Privilege>) : Privilege {
    constructor(vararg privileges: Privilege) : this(privileges.asList())

    override suspend fun invoke(role: Role): List<Approval> {
        if (privileges.isEmpty())
            return listOf(Approval(true, role.error))

        var firstSuccess: Approval? = null

        for (privilege in privileges) {
            val approvals = privilege(role)

            if (privileges.isEmpty())
                return listOf(Approval(false, role.error))

            for (approval in approvals)
                if (!approval.value)
                    return listOf(approval)

            firstSuccess = firstSuccess ?: approvals.firstOrNull()
        }

        return listOf(firstSuccess ?: Approval(true, role.error))
    }
}
