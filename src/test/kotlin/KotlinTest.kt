package org.cufy.openperm

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

// Model module

data class Grant(
    val ownerId: String,
    val value: String
)

data class MyEntity(
    val id: String,
    val ownerId: String,
    val isPublic: Boolean,
    val grants: List<Grant>
)

data class MyAccount(
    val id: String,
    val isSuspended: Boolean,
    val isAdmin: Boolean
)

// Auth module

data class MyGrantRole(
    val value: String,
    val entity: MyEntity,
    override val error: Throwable? = null
) : Role()

data class MyOwnershipRole(
    val entity: MyEntity,
    override val error: Throwable? = null
) : Role()

fun createMyAccountPrivilege(account: MyAccount) =
    Privilege { role ->
        when {
            account.isSuspended -> Privilege(false, Denial("account_suspended"))
            account.isAdmin -> Privilege(true)
            role is MyGrantRole -> Privilege(role.entity.grants.any {
                it.ownerId == account.id && it.value == role.value
            })
            role is MyOwnershipRole -> Privilege(
                role.entity.ownerId == account.id
            )
            else -> Privilege(false)
        }
    }

// Permission module

object MyEntityPermit {
    val READ = Permit<MyEntity> {
        Permit(MyGrantRole(value = "READ", entity = it, error = Denial("entity_read")))
    }
    val WRITE = Permit<MyEntity> {
        Permit(MyGrantRole(value = "WRITE", entity = it, error = Denial("entity_write")))
    }
    val OWNER = Permit<MyEntity> {
        Permit(MyOwnershipRole(entity = it, error = Denial("entity_owner")))
    }
}

object MyEntityPrerequisite {
    val PUBLIC = Permission<MyEntity> { _, target ->
        Permission(target.isPublic, Denial("entity_not_public"))
    }
}

object MyEntityPermission {
    object Read {
        val OWNER = SomePermission(
            Permission(MyEntityPermit.OWNER)
        )
        val GRANTED = SomePermission(
            Permission(MyEntityPermit.READ),
            OWNER
        )
        val ANONYMOUS = SomePermission(
            MyEntityPrerequisite.PUBLIC,
            GRANTED
        )
    }

    object Write {
        val OWNER = SomePermission(
            Permission(MyEntityPermit.OWNER)
        )
        val GRANTED = SomePermission(
            Permission(MyEntityPermit.WRITE),
            OWNER
        )
        val ANONYMOUS = SomePermission(
            MyEntityPrerequisite.PUBLIC,
            GRANTED
        )
    }
}

// Test

class KotlinTest {
    @Test
    fun main() {
        runBlocking {
            suspendMain()
        }
    }

    suspend fun suspendMain() {
        val myAccount1 = MyAccount(
            id = "MyAccount.1",
            isSuspended = false,
            isAdmin = true
        )
        val myAccount2 = MyAccount(
            id = "MyAccount.2",
            isSuspended = false,
            isAdmin = false
        )
        val myEntity1 = MyEntity(
            id = "MyEntity.1",
            ownerId = myAccount1.id,
            isPublic = false,
            grants = listOf()
        )
        val myEntity2 = MyEntity(
            id = "MyEntity.2",
            ownerId = myAccount2.id,
            isPublic = true,
            grants = listOf()
        )

        val myPrivilege1 = createMyAccountPrivilege(myAccount1)
        val myPrivilege2 = createMyAccountPrivilege(myAccount2)

        requirePermission(MyEntityPermission.Read.GRANTED, myPrivilege1, myEntity2)
    }
}
