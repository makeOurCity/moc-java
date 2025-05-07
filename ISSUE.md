# Maven Central と Gradle 対応の追加

## 概要
moc-javaライブラリをMaven CentralリポジトリとGradleで簡単に使用できるようにするための機能追加を提案します。

## 目的
- オープンソースライブラリとしての使いやすさを向上させる
- 依存関係管理を簡素化する
- より多くの開発者に利用してもらえるようにする

## 提案内容

### 1. Maven Central対応
- Maven Central公開に必要なメタデータの追加
  - ライセンス情報
  - 開発者情報
  - SCM情報
- Javadocとソースの添付設定
- GPG署名の設定
- Sonatype OSSリポジトリの設定

### 2. Gradle対応
- `build.gradle`ファイルの作成
- `settings.gradle`ファイルの作成
- Maven Centralと同等のメタデータ設定

### 3. 使用方法ドキュメント
- MavenとGradleでの使用方法を説明するドキュメントの追加

## 実装後の使用例

### Maven
```xml
<dependency>
    <groupId>city.makeour</groupId>
    <artifactId>moc</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'city.makeour:moc:1.0.0'
```

## 必要なステップ
1. Sonatypeアカウントの作成
2. GPG鍵の設定
3. ビルド設定の更新
4. ドキュメントの作成
5. テストとデプロイ

この機能追加により、moc-javaライブラリの利用がより簡単になり、オープンソースプロジェクトとしての価値が高まると考えています。
