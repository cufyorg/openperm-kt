# Openperm [![](https://jitpack.io/v/org.cufy/openperm-kt.svg)](https://jitpack.io/#org.cufy/openperm-kt)

A general purpose permission management library.

> This is a kotlin version of the
>
original [openperm library](https://github.com/cufyorg/openperm-js)

### Install

The main way of installing this library is
using `jitpack.io`

```kts
repositories {
    // ...
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Replace TAG with the desired version
    implementation("org.cufy:openperm-kt:TAG")
}
```

### Components

#### Approval

An approval is the result of validating some condition.

<br/>

A simple approval construction would look like this:

```kotlin
val Approved = Approval(true, error = Denial("Approved"))
val Denied = Approval(false, error = Denial("Denied"))
```

#### Role

A role is an open class that is implemented by the
application and is the way to pass data to a `Privilege`.

<br/>

A simple role implementation would look like this:

```kotlin
data class MyRole(
    val target: MyTarget,

    val error: Throwable? = null
) : Role()
```

#### Permit

A permit is a `Role` factory that construct `Role`s for a
given entity.

<br/>

A simple permit composition would look like this:

```kotlin
val MyPermit = Permit<String>(
    Permit(MyRole("")),
    Permit { Permit(MyRole(it)) }
)
```

#### Privilege

A privilege is a composed function that indicate if a `Role`
is or is not met.

<br/>

A simple privilege composition would look like this:

```kotlin
fun createPrivilege(actor: MyActor) = Privilege(
    Privilege(true),
    Privilege(Approval(true)),
    Privilege { role ->
        when (role) {
            is MyActRole -> Privilege(actor.canAct(role.actTarget))
            is MyViewRole -> Privilege(actor.canView(role.viewTarget))
            else -> Privilege(false)
        }
    }
)
```

#### Permission

A permission is a composed function that indicates if
some condition is or is not met for a given entity and
by a given `Privilege`

<br/>

A simple permission composition would look like this:

```kotlin
val MyPermission = Permission<MyTarget>(
    Permission(true),
    Permission(Approval(true)),
    Permission(MyPermit),
    Permission { privilege, target ->
        when {
            target.isPublic() -> Permission(true)
            else -> MyNotPublicTargetPermission
        }
    }
)
```

### Phases

The permission calculation is split into two phases:

#### Privilege construction

At this phase, the target is to construct a `Privilege` that
takes a `Role` and outputs a list of `Approval`s.

Usually, this phase is done after identifying the actor.

For example:

```kotlin
fun createAccountPrivilege(actor: MyActor) =
    Privilege { role ->
        when (role) {
            is MyCanPerformOnRole -> Privilege(actor.canPerformOn(
                role.target))
            else -> Privilege(false)
        }
    }
```

#### Permission Validation

At this phase, the target is to output a list of `Approvals`
s
for a given entity against a given privilege.

Usually, this phase is done before performing an action.

For example:

```kotlin
fun performAction(privilege: Privilege, target: MyTarget) {
    requirePermission(MyCanPerformOnPermission,
        privilege,
        target)
}
```

### Examples

The following are examples of using this library

- [KotlinTest](src/test/kotlin/KotlinTest.kt)
  The main test file

- [KtorGraphQLTemplate](https://github.com/cufyorg/ktor-graphql-template)
  A template that uses this library
