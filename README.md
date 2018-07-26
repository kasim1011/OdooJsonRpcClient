OdooJsonRpcClient
========

Odoo Json RPC client for Android.

This project is developed against **Odoo 10.0 Community Edition** and it's compatible with **Odoo 11.0**. It may not work properly against older versions of Odoo.

Configure Odoo host address, Project website, Privacy policy and Contact email from [configs.xml](https://github.com/kasim1011/OdooJsonRpcClient/blob/master/app/src/main/res/values/configs.xml)

Get the Odoo Json-rpc request collection for [Postman](https://github.com/kasim1011/OdooJsonRpcClient/blob/master/OdooJsonRpc.postman_collection.json)

How to use
=======

Odoo specific methods can be access using [singleton object](https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations) `Odoo`.
Login Account related functionality can be access using `AppcompatActivity`'s [Extension functions](https://kotlinlang.org/docs/reference/extensions.html#extension-functions).
`Authentication` as well as `Sessions` are managed inside application's `core` module. You should not use any `session` related methods anywhere in application, It may lead to unexpected behaviour of application.

Odoo specific methods are following:

SearchRead
==========
**Request**
```kotlin
Odoo.searchRead(model = "res.partner", fields = listOf(
        "id", "name", "email", "company_name"
), domain = listOf(listOf("customer", "=", true)), offset = 0, limit = 4, sort = "name ASC") {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val searchRead = response.body()!!
            if (searchRead.isSuccessful) {
                val result = searchRead.result
                // use gson to convert records (jsonArray) to list of pojo
                // ...
            } else {
                // Odoo specific error
                Timber.w("searchRead() failed with ${searchRead.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": {
    "length": 25,
    "records": [
      {
        "id": 9,
        "name": "Agrolait",
        "email": "agrolait@yourcompany.example.com",
        "company_name": false
      },
      {
        "id": 31,
        "name": "Ayaan Agarwal",
        "email": "ayaan.agarwal@bestdesigners.example.com",
        "company_name": false
      },
      {
        "id": 37,
        "name": "Benjamin Flores",
        "email": "benjamin.flores@nebula.example.com",
        "company_name": false
      },
      {
        "id": 13,
        "name": "Camptocamp",
        "email": "camptocamp@yourcompany.example.com",
        "company_name": false
      }
    ]
  }
}
```

Load
==========
**Request**
```kotlin
Odoo.load(id = 1, model = "res.partner", fields = listOf()) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val load = response.body()!!
            if (load.isSuccessful) {
                val result = load.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("load() failed with ${load.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": {
    "value": {
      "id": 1,
      "name": "YourCompany",
      "display_name": "YourCompany",
      "date": false,
      "title": false,
      "parent_id": false,
      "child_ids": [
        42,
        43
      ],
      "ref": false,
      "lang": "en_US",
      "tz": false,
      "user_id": false,
      "vat": false,
      "bank_ids": [
        
      ],
      "website": "http://www.example.com",
      "opportunity_count": 0,
      "meeting_count": 0,
      "__last_update": "2018-07-23 18:10:57"
    }
  }
}
```

CallKw
==========
**Request**
```kotlin
Odoo.callKw(model = "res.users", method = "has_group", args = listOf("base.group_user")) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val callKw = response.body()!!
            if (callKw.isSuccessful) {
                val result = callKw.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("callkw() failed with ${callKw.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": true
}
```

Create
==========
**Request**
```kotlin
Odoo.create(model = "res.partner", keyValues = mapOf(
        "name" to "Kasim Rangwala", "email" to "rangwalakasim@live.in"
)) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val create = response.body()!!
            if (create.isSuccessful) {
                val result = create.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("create() failed with ${create.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": 45
}
```

Read
=======
**Request**
```kotlin
Odoo.read(model = "res.partner", id = 13, fields = listOf("id", "name", "email")) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val read = response.body()!!
            if (read.isSuccessful) {
                val result = read.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("read() failed with ${read.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": [
    {
      "id": 13,
      "name": "Camptocamp",
      "email": "camptocamp@yourcompany.example.com"
    }
  ]
}
```

NameSearch
==========
**Request**
```kotlin
Odoo.nameSearch(model = "res.partner", name = "Delta PC", args = listOf(), operator = "ilike", limit = 0) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val nameSearch = response.body()!!
            if (nameSearch.isSuccessful) {
                val result = nameSearch.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("nameSearch() failed with ${nameSearch.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": [
    [
      11,
      "Delta PC"
    ],
    [
      28,
      "Delta PC, Charlie Bernard"
    ],
    [
      29,
      "Delta PC, Jessica Dupont"
    ],
    [
      41,
      "Delta PC, Kevin Clarke"
    ],
    [
      40,
      "Delta PC, Morgan Rose"
    ],
    [
      17,
      "Delta PC, Richard Ellis"
    ],
    [
      35,
      "Delta PC, Robert Anderson"
    ],
    [
      39,
      "Delta PC, Robin Smith"
    ]
  ]
}
```

NameGet
=======
**Request**
```kotlin
Odoo.nameGet(model = "res.partner", ids = listOf(1, 3)) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val nameGet = response.body()!!
            if (nameGet.isSuccessful) {
                val result = nameGet.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("nameGet() failed with ${nameGet.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": [
    [
      1,
      "YourCompany"
    ],
    [
      3,
      "YourCompany, Administrator"
    ]
  ]
}
```

SearchCount
===========
**Request**
```kotlin
Odoo.searchCount(model = "res.partner", args = listOf(listOf("name", "ilike", "kasim rangwala"))) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val searchCount = response.body()!!
            if (searchCount.isSuccessful) {
                val result = searchCount.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("searchCount() failed with ${searchCount.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": 2
}
```

Unlink
=======
**Request**
```kotlin
Odoo.unlink(model = "res.partner", ids = listOf(47, 48)) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val unlink = response.body()!!
            if (unlink.isSuccessful) {
                val result = unlink.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("unlink() failed with ${unlink.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": true
}
```

Write
=======
**Request**
```kotlin
Odoo.write(model = "res.partner", ids = listOf(45, 46),
        keyValues = mapOf("name" to "Kasim3 Rangwala1", "email" to "rangwalakasim@live.in")) {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val write = response.body()!!
            if (write.isSuccessful) {
                val result = write.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("write() failed with ${write.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": true
}
```

Modules
=======
**Request**
```kotlin
Odoo.modules {
    onSubscribe { disposable ->
        compositeDisposable.add(disposable)
    }

    onNext { response ->
        if (response.isSuccessful) {
            val modules = response.body()!!
            if (modules.isSuccessful) {
                val result = modules.result
                // ...
            } else {
                // Odoo specific error
                Timber.w("modules() failed with ${modules.errorMessage}")
            }
        } else {
            Timber.w("request failed with ${response.code()}:${response.message()}")
        }
    }

    onError { error ->
        error.printStackTrace()
    }

    onComplete { }
}
```
**Result**
```json
{
  "result": [
    "base",
    "web",
    "bus",
    "web_tour",
    "mail",
    "sales_team",
    "calendar",
    "web_planner",
    "contacts",
    "crm",
    "auth_signup",
    "base_import",
    "iap",
    "sms",
    "web_diagram",
    "web_editor",
    "web_kanban_gauge",
    "web_settings_dashboard"
  ]
}
```

License
=======
    MIT License

    Copyright (c) 2018 Kasim Rangwala

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
