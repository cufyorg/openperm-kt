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
@file:JvmName("OpenpermCompat")

package org.cufy.openperm

/*
    This file contains utilities that are present
    in the nodejs version of this library. These
    utilities don't quite follow kotlin style.
    All the utilities in this file are deprecated
    but will not be removed in near future.
    Only functions with replacements will be
    annotated with the `@Deprecated` annotation.
 */

/* ================================================================ */
/* =========================   APPROVAL   ========================= */
/* ================================================================ */

/**
 * Return a generic successful approval.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Approval(true)",
        "org.cufy.openperm.Approval"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
val Approval.Companion.GRANT
    get() = Approval(false, Denial("Approval.GRANT"))

/**
 * Return a generic failure approval.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Approval(false)",
        "org.cufy.openperm.Approval"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
val Approval.Companion.DENY
    get() = Approval(false, Denial("Approval.DENY"))

/* ================================================================ */
/* =========================  PERMISSION  ========================= */
/* ================================================================ */

/**
 * Check the given permission.
 *
 * @param permission the permission to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return true, if the privilege is permissioned the given permission for the given target.
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "testPermission(permission, privilege, target)",
        "org.cufy.openperm.testPermission"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> isPermissioned(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
) = test(permission, privilege, target)

/**
 * Check the given permission and throw the error if it fails.
 *
 * @param permission the permission to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permission for.
 * @author LSafer
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "require(permission, privilege, target)",
        "org.cufy.openperm.require"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> requirePermission(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
) = require(permission, privilege, target)

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
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "test(permission, privilege, target)",
        "org.cufy.openperm.test"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> testPermission(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
) = test(permission, privilege, target)

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
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "check(permission, privilege, target)",
        "org.cufy.openperm.check"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> checkPermission(
    permission: Permission<T>,
    privilege: Privilege,
    target: T
) = check(permission, privilege, target)

/**
 * Return a permission that checks the given `permissions`.
 *
 * If the permissions array is empty, the returned permission will always
 * evaluate to `true`.
 *
 * If at least one permission evaluates to false, the permission will
 * evaluate to `false`.
 *
 * If at least one permission doesn't emit an approval, the permission will
 * evaluate to `false`.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "EveryPermission(permissions)",
        "org.cufy.openperm.EveryPermission"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun <T> Permission.Companion.every(
    vararg permissions: Permission<T>
) = EveryPermission(*permissions)

/**
 * Return a permission that checks the given `permissions`.
 *
 * If the permissions array is empty, the returned permission will always
 * evaluate to `false`.
 *
 * If at least one permission evaluates to true, the permission will
 * evaluate to `true`.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "SomePermission(permissions)",
        "org.cufy.openperm.SomePermission"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun <T> Permission.Companion.some(
    vararg permissions: Permission<T>
) = SomePermission(*permissions)

/**
 * Create a permission that returns the result of checking the given permit.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Permission(permit)",
        "org.cufy.openperm.Permission"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun <T> Permission.Companion.create(
    permit: Permit<T>
) = Permission { privilege, target ->
    Permission(check(permit, privilege, target))
}

/**
 * Create a new permission that returns the result of invoking the given
 * permission with the target being the result of invoking the given mapper
 * with the target given to it.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Permission(permission, mapper = mapper)",
        "org.cufy.openperm.Permission"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun <T, U> Permission.Companion.map(
    permission: Permission<U>,
    mapper: suspend (T) -> U
) = object : Permission<T> {
    override suspend fun invoke(privilege: Privilege, target: T): List<Approval> {
        return permission(privilege, mapper(target))
    }
}

/* ================================================================ */
/* =========================  PRIVILEGE   ========================= */
/* ================================================================ */

/**
 * Check the given privilege.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return true, if the privilege has approval for the given role.
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "test(privilege, role)",
        "org.cufy.openperm.test"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun isPrivileged(
    privilege: Privilege,
    role: Role
) = test(privilege, role)

/**
 * Check the given privilege and throw the error if it fails.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return the role.
 * @author LSafer
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "require(privilege, role)",
        "org.cufy.openperm.require"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun requirePrivilege(
    privilege: Privilege,
    role: Role
) = require(privilege, role)

/**
 * Check the given privilege.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return true, if the privilege has approval for the given role.
 * @author LSafer
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "test(privilege, role)",
        "org.cufy.openperm.test"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun testPrivilege(
    privilege: Privilege,
    role: Role
) = test(privilege, role)

/**
 * Check the given privilege.
 *
 * @param privilege the privilege to be checked.
 * @param role the role to check the privilege for.
 * @return an approval object.
 * @author LSafer
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "check(privilege, role)",
        "org.cufy.openperm.check"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun checkPrivilege(
    privilege: Privilege,
    role: Role
) = check(privilege, role)

/**
 * Return a privilege that always succeed.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Privilege(true)",
        "org.cufy.openperm.Privilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
val Privilege.Companion.GRANT
    get() = Privilege { role ->
        Privilege(Approval(true, role.error))
    }

/**
 * A privilege that always fails.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Privilege(false)",
        "org.cufy.openperm.Privilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
val Privilege.Companion.DENY
    get() = Privilege { role ->
        Privilege(Approval(false, role.error))
    }

/**
 * Return a privilege that always succeeds when `success`
 * is true and always fails otherwise.
 *
 * @since 1.0.0
 */
@Suppress("DEPRECATION")
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Privilege(success)",
        "org.cufy.openperm.Privilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun Privilege.Companion.result(success: Boolean) =
    if (success) Privilege.GRANT else Privilege.DENY

/**
 * Return a privilege that checks the given `privileges`.
 *
 * If the privileges array is empty, the returned privilege will always
 * evaluate to `true`.
 *
 * If at least one privilege evaluates to false, the privilege will
 * evaluate to `false`.
 *
 * If at least on privilege doesn't emit an approval, the privilege will
 * evaluate to `false`.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "EveryPrivilege(privileges)",
        "org.cufy.openperm.EveryPrivilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun Privilege.Companion.every(
    vararg privileges: Privilege
) = EveryPrivilege(*privileges)

/**
 * Return a privilege that checks the given `privileges`.
 *
 * If the privileges array is empty, the returned privilege will always
 * evaluate to `false`.
 *
 * If at least one privilege evaluates to true, the privilege will
 * evaluate to `true`.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "SomePrivilege(privileges)",
        "org.cufy.openperm.SomePrivilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun Privilege.Companion.some(
    vararg privileges: Privilege
) = SomePrivilege(*privileges)

/**
 * Return a privilege that caches resolved roles.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "CachedPrivilege(privilege)",
        "org.cufy.openperm.CachedPrivilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun Privilege.Companion.cached(
    privilege: Privilege
) = CachedPrivilege(privilege)

/**
 * Return a privilege that invokes the given `privilegeProvider` with a
 * privilege that redirects to the result of invoking it.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Privilege",
        "org.cufy.openperm.Privilege"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun Privilege.Companion.withSelf(
    privilegeProvider: (Privilege) -> Privilege
): Privilege = run {
    object : Privilege {
        override suspend fun invoke(role: Role): List<Approval> {
            return privilegeProvider(this)(role)
        }
    }
}

/* ================================================================ */
/* =========================    PERMIT    ========================= */
/* ================================================================ */

/**
 * Check the given permit.
 *
 * @param permit the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @return true, if the privilege is permitted the given permit for the given target.
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "test(permit, privilege, target)",
        "org.cufy.openperm.test"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> isPermitted(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = test(permit, privilege, target)

/**
 * Check the given permit and throw the error if it fails.
 *
 * @param permit the permit to be checked.
 * @param privilege the privilege.
 * @param target the target to check the permit for.
 * @author LSafer
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "require(permit, privilege, target)",
        "org.cufy.openperm.require"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> requirePermit(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = require(permit, privilege, target)

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
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "test(permit, privilege, target)",
        "org.cufy.openperm.test"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> testPermit(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = test(permit, privilege, target)

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
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "check(permit, privilege, target)",
        "org.cufy.openperm.check"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
suspend fun <T> checkPermit(
    permit: Permit<T>,
    privilege: Privilege,
    target: T
) = check(permit, privilege, target)

/**
 * Create a permit that returns the result of invoking the given permit with
 * the target being the result of invoking the given mapper with the target given to it.
 *
 * @since 1.0.0
 */
// <editor-fold desc="@Deprecated">
@Deprecated(
    "openperm original function",
    ReplaceWith(
        "Permit(permit, mapper = mapper)",
        "org.cufy.openperm.Permit"
    ),
    DeprecationLevel.WARNING
)
// </editor-fold>
fun <T, U> Permit.Companion.map(
    permit: Permit<U>,
    mapper: suspend (T) -> U
) = object : Permit<T> {
    override suspend fun invoke(target: T): List<Role> {
        return permit(mapper(target))
    }
}
