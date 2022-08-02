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
 * An interface to standardize permissions.
 *
 * Note: this interface is completely optional and
 * using this library without using this interface
 * will always be an option.
 *
 * @author LSafer
 * @since 1.2.0
 */
@Suppress("PropertyName")
interface Permissions<T> {
    /**
     * Check for anonymous read ability for [T].
     *
     * @since 1.2.0
     */
    val AnonymousRead: Permission<T> get() = Read

    /**
     * Check for anonymous write ability for [T].
     *
     * @since 1.2.0
     */
    val AnonymousWrite: Permission<T> get() = Write

    /**
     * Check for read ability for [T].
     *
     * @since 1.2.0
     */
    val Read: Permission<T> get() = OwnerRead

    /**
     * Check for write ability for [T].
     *
     * @since 1.2.0
     */
    val Write: Permission<T> get() = OwnerWrite

    /**
     * Check for owner-level read ability for [T].
     *
     * @since 1.2.0
     */
    val OwnerRead: Permission<T> get() = Permission(false)

    /**
     * Check for owner-level write ability for [T].
     *
     * @since 1.2.0
     */
    val OwnerWrite: Permission<T> get() = Permission(false)
}
