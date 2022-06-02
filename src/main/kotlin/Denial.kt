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
 * An exception thrown when a permission got denied.
 *
 * @author LSafer
 * @since 1.0.0
 */
open class Denial : RuntimeException {
    constructor() : super()

    constructor(message: String?) :
            super(message)

    constructor(message: String?, cause: Throwable?) :
            super(message, cause)

    constructor(cause: Throwable?) :
            super(cause)

    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )

    companion object {
        private const val serialVersionUID: Long = -2052115477586379087L

        /**
         * An error to be thrown when a permission
         * is denied but no error was specified.
         *
         * @author LSafer
         * @since 1.0.0
         */
        val Unspecified get() = Denial("Unspecified")

        /**
         * An error to be thrown when a permission
         * check operation produced no results.
         *
         * @author LSafer
         * @since 1.0.0
         */
        val NoResults get() = Denial("No Result")

        /**
         * An error to be thrown when performing a
         * check operation with no permissions.
         *
         * @author LSafer
         * @since 1.0.0
         */
        val NoChecklist get() = Denial("No Checklist")
    }
}
