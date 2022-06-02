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
 * A type representing the approval results.
 *
 * @author LSafer
 * @since 1.0.0
 */
open class Approval(
    /**
     * True, if the access is approved. False, otherwise.
     *
     * @author LSafer
     * @since 1.0.0
     */
    val value: Boolean,
    /**
     * The error that caused the approval to fail.
     *
     * @author LSafer
     * @since 1.0.0
     */
    val error: Throwable? = null,
) {
    companion object
}
