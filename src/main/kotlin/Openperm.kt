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
@file:JvmName("Openperm")

package org.cufy.openperm

/**
 * Check the given privilege and throw the error if it fails.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return the role.
 * @author LSafer
 * @since 1.0.0
 */
suspend fun requirePrivilege(
    privilege: Privilege,
    role: Role
): Role {
    val approval = checkPrivilege(privilege, role)

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
suspend fun testPrivilege(
    privilege: Privilege,
    role: Role
): Boolean {
    val approval = checkPrivilege(privilege, role)

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
suspend fun checkPrivilege(
    privilege: Privilege,
    role: Role
): Approval {
    val approvals = privilege(role)

    if (approvals.isEmpty())
        return Approval(false, role.error)

    for (approval in approvals)
        if (!approval.value)
            return approval

    return approvals[0]
}

/**
 * Check the given permit and throw the error if it fails.
 *
 * @param permit the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @author LSafer
 * @since 1.0.0
 */
suspend fun <T> requirePermit(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
): T {
    val approval = checkPermit(permit, privilege, target)

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
 * @since 1.0.0
 */
suspend fun <T> testPermit(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
): Boolean {
    val approval = checkPermit(permit, privilege, target)

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
 * @since 1.0.0
 */
suspend fun <T> checkPermit(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
): Approval {
    val roles = permit(target)

    if (roles.isEmpty())
        return Approval(false, Denial.NoChecklist)

    for (role in roles) {
        val approvals = privilege(role)

        if (approvals.isEmpty())
            return Approval(false, role.error)

        for (approval in approvals)
            if (!approval.value)
                return approval
    }

    return Approval(false, roles[0].error)
}

/**
 * Check the given permission and throw the error if it fails.
 *
 * @param permission the permission to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permission for.
 * @author LSafer
 * @since 1.0.0
 */
suspend fun <T> requirePermission(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
): T {
    val approval = checkPermission(permission, privilege, target)

    if (!approval.value)
        throw approval.error ?: Denial.Unspecified

    return target
}

/**
 * Check the given permission.
 *
 * @param permission the permission to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return true, if the privilege is permissioned the given permission for the given target.
 * @author LSafer
 * @since 1.0.0
 */
suspend fun <T> testPermission(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
): Boolean {
    val approval = checkPermission(permission, privilege, target)

    return approval.value
}

/**
 * Check the given permission.
 *
 * @param permission the permission to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permission for.
 * @return an approval object.
 * @author LSafer
 * @since 1.0.0
 */
suspend fun <T> checkPermission(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
): Approval {
    val approvals = permission(privilege, target)

    if (approvals.isEmpty())
        return Approval(false, Denial.NoResults)

    for (approval in approvals)
        if (!approval.value)
            return approval

    return approvals[0]
}
