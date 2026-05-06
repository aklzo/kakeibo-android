# Retrofit2 メモ

## 基本構成

Retrofit は HTTP クライアントのライブラリ。  
インターフェースにアノテーションでエンドポイントを定義し、実装を自動生成する。

```kotlin
// インターフェース定義
interface ApiService {
    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @Query("month") month: String?
    ): Response<TransactionsListResponse>
}

// Retrofit インスタンス生成と紐付け
val retrofit = Retrofit.Builder()
    .baseUrl("https://example.run.app/api/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val service = retrofit.create(ApiService::class.java)
```

---

## 主なアノテーション

### HTTP メソッド

```kotlin
@GET("transactions")          // GET リクエスト
@POST("transactions")         // POST リクエスト
@PATCH("transactions/{id}")   // PATCH リクエスト
@DELETE("transactions/{id}")  // DELETE リクエスト
```

### パラメータ

```kotlin
@Header("Authorization") token: String     // リクエストヘッダー
@Query("month") month: String?             // クエリパラメータ (?month=2025-05)
@Path("id") id: Int                        // パスパラメータ ({id} の置換)
@Body request: CreateTransactionRequest    // リクエストボディ（JSON）
```

---

## レスポンス型

`Response<T>` にすると HTTP ステータスコードや本文を確認できる。

```kotlin
val response: Response<TransactionsListResponse> = service.getTransactions(...)

if (response.isSuccessful) {
    val body = response.body()   // T? を返す（204 No Content などは null）
} else {
    val code = response.code()   // 400, 401, 404 など
}
```

`Response<Unit>` は DELETE などボディが不要なエンドポイントに使う。

---

## null フィールドの JSON シリアライズ

デフォルトの Gson は `null` のフィールドを JSON に含めない。  
PATCH エンドポイントで `"memo": null` を明示的に送る必要があるため、`serializeNulls()` を設定する。

```kotlin
val gson = GsonBuilder().serializeNulls().create()
val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create(gson))
    ...
```

これがないと `{ "amount": 2000 }` となり、`memo` フィールドが消えてサーバー側で null 扱いされない。

---

## スネークケースへの対応

API のレスポンスが `created_at` のようなスネークケースの場合、`@SerializedName` でマッピングする。

```kotlin
data class TransactionData(
    val id: Int,
    val name: String,
    val amount: Int,
    @SerializedName("created_at") val createdAt: String
)
```

---

## baseUrl の末尾スラッシュ

Retrofit の `baseUrl` は**末尾にスラッシュが必須**。  
相対パスのエンドポイント（`@GET("transactions")` など）を正しく解決するため。

`local.properties` に末尾スラッシュを含めるか、コード側で補完する。

```kotlin
val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
```

---

## シングルトンパターン

`ApiClient` を `object` で定義し、`lazy` で初回アクセス時のみ Retrofit を構築する。

```kotlin
object ApiClient {
    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
```
