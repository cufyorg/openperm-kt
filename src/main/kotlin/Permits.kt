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
 * An interface to standardize permits.
 *
 * Note: this interface is completely optional and
 * using this library without using this interface
 * will always be an option.
 *
 * @author LSafer
 * @since 1.2.0
 */
@Suppress("PropertyName")
interface Permits<T> {
    /**
     * Check for a written permit for reading [T].
     *
     * @since 1.2.0
     */
    val Read: Permit<T> get() = Permit(Denial("READ"))

    /**
     * Check for a written permit for writing [T].
     *
     * @since 1.2.0
     */
    val Write: Permit<T> get() = Permit(Denial("WRITE"))

    /**
     * Check for a written permit for owning [T].
     *
     * @since 1.2.0
     */
    val Owner: Permit<T> get() = Permit(Denial("OWNER"))
}
