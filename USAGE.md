# MoC Java Client Usage Guide

このドキュメントでは、MoC Java クライアントライブラリを Maven と Gradle で使用する方法について説明します。

## Maven での使用方法

Maven プロジェクトで MoC Java クライアントを使用するには、`pom.xml` に以下の依存関係を追加してください：

```xml
<dependency>
    <groupId>city.makeour</groupId>
    <artifactId>moc</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Gradle での使用方法

Gradle プロジェクトで MoC Java クライアントを使用するには、`build.gradle` に以下の依存関係を追加してください：

### Gradle (Groovy DSL)

```groovy
implementation 'city.makeour:moc:1.0.0'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("city.makeour:moc:1.0.0")
```

## 基本的な使用例

```java
import city.makeour.moc.MocClient;

public class Example {
    public static void main(String[] args) {
        // クライアントの初期化
        MocClient client = new MocClient();
        
        // 認証情報の設定
        client.setMocAuthInfo("your-cognito-user-pool-id", "your-cognito-client-id");
        
        // 認証
        client.auth("username", "password");
        
        // エンティティの操作
        // 詳細は examples ディレクトリのサンプルコードを参照してください
    }
}
```

詳細な使用例については、[examples ディレクトリ](./src/main/java/city/makeour/moc/examples/)を参照してください。

## Maven Central Repository への公開

このライブラリは Maven Central Repository で公開されており、追加のリポジトリ設定なしで使用できます。

## ライセンス

このライブラリは MIT ライセンスの下で公開されています。詳細については、[LICENSE](./LICENSE) ファイルを参照してください。
