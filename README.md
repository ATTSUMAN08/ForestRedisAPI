# ForestRedisAPI
![badge](https://img.shields.io/github/v/release/ATTSUMAN08/ForestRedisAPI)
[![badge](https://jitpack.io/v/ATTSUMAN08/ForestRedisAPI.svg)](https://jitpack.io/#ATTSUMAN08/ForestRedisAPI)
![badge](https://img.shields.io/github/downloads/ATTSUMAN08/ForestRedisAPI/total)
![badge](https://img.shields.io/github/last-commit/ATTSUMAN08/ForestRedisAPI)
![badge](https://img.shields.io/badge/platform-spigot%20%7C%20bungeecord%20%7C%20velocity-lightgrey)
[![badge](https://img.shields.io/github/license/ATTSUMAN08/ForestRedisAPI)](https://github.com/ATTSUMAN08/ForestRedisAPI/blob/master/LICENSE.txt)

Jedisライブラリに基づいたシンプルなSpigot&Bungee用Redis APIです。ForestRedisAPIは、開発者が簡単なAPI呼び出しやイベントを使用して、サーバー間の通信を快適に維持することを可能にします。**BungeeCord、PaperおよびVelocityサーバーをサポートしています。**

## 目次

* [開始手順](#開始手順)
* [チャンネルの購読](#チャンネルの購読)
* [メッセージ/オブジェクトの送信](#メッセージオブジェクトの送信)
* [イベントと受信メッセージ](#イベントと受信メッセージ)
* [ライセンス](#ライセンス)

## 開始手順

サーバーにForestRedisAPIプラグインがインストールされていることを確認してください。

### ForestRedisAPIをプロジェクトに追加する

[![badge](https://jitpack.io/v/ATTSUMAN08/ForestRedisAPI.svg)](https://jitpack.io/#ForestTechMC/ForestRedisAPI)

まず、ForestRedisAPIの依存関係を設定する必要があります。**VERSION**をリリースバージョンに置き換えてください。

<details>
    <summary>Mavenの場合</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.ATTSUMAN08</groupId>
        <artifactId>ForestRedisAPI</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
</details>

<details>
    <summary>Gradleの場合</summary>

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.ATTSUMAN08:ForestRedisAPI:VERSION'
}
```
</details>

### プラグイン設定

ForestRedisAPIを適切に機能させるには、プラグインに(ソフト)依存する必要があります。ForestRedisAPIの必須依存関係を設定するか、オプションで使用するためにソフト依存関係を設定してください。

<details>
    <summary>plugin.yml (Spigot)</summary>

```yaml
# 必須依存関係
depend: [ForestRedisAPI]
# オプション依存関係
softdepend: [ForestRedisAPI]
```

</details>

<details>
    <summary>bungee.yml (BungeeCord)</summary>

```yaml
# 必須依存関係
depends: [ForestRedisAPI]
# オプション依存関係
softDepends: [ForestRedisAPI]
```

</details>

## チャンネルの購読

Redisサーバーからデータを受信するには、選択したチャンネルを購読する必要があります。以下のように簡単に呼び出すことができます。

```java
// チャンネルが購読済みかどうかを確認できます
if(RedisManager.getAPI().isSubscribed("MyChannel")){
        this.log().warning("チャンネル 'MyChannel' はすでに購読されています！");
        return;
}

// 必要なだけ多くのチャンネルを購読できます。
// すでに購読済みのチャンネルはスキップされます。
RedisManager.getAPI().subscribe("MyChannel1","MyChannel2","MyChannel3");
```

## メッセージ/オブジェクトの送信

Redisサーバーにメッセージやオブジェクトを簡単に送信できます。送信先チャンネルを購読する必要はありません。

```java
// String形式の単純なメッセージを送信するには、#publishMessageメソッドを使用します。
RedisManager.getAPI().publishMessage("MyChannel1","こんにちは、お元気ですか？");

// 任意のオブジェクトも送信できます。それらはJSONを使用してシリアライズされます。
RedisManager.getAPI().publishObject("MyChannel1",new MyObject());
```

## イベントと受信メッセージ

ForestRedisAPIを使用すると、BukkitやBungeeのリスナーを使用してRedisからデータを取得できます。**ただし、BungeeとSpigotではイベント名が同じなので、正しいイベントを選択してください！**

```java
// BungeeCordの場合はbungeeイベントをインポートしてください！！！

import cz.foresttech.forestredis.spigot.events.RedisMessageReceivedEvent;

public class MyListener implements Listener {

    @EventHandler
    public void onRedisMessageReceived(RedisMessageReceivedEvent event) {

        // メッセージがこのサーバーから送信されたかどうか。
        // ForestRedisAPIのconfig.ymlのserverIdentifierを使用します。
        boolean isSelfMessage = event.isSelfSender();

        // チャンネル名。事前に購読する必要があります。
        String channel = event.getChannel();

        // 送信元サーバーの識別子。
        String senderServerId = event.getSenderIdentifier();
        
        // メッセージが送信された日時
        long timestamp = event.getTimeStamp();

        // 受信したメッセージのテキスト。
        String messageText = event.getMessage();

        // JSONから任意のオブジェクトを解析します。#getMessage()の代わりに使用できます。
        // 解析できない場合は'null'を返します。
        MyObject myObject = event.getMessageObject(MyObject.class);

    }

}
```

## ライセンス
ForestRedisAPIはMITライセンスの下でライセンスされています。詳細は[`LICENSE.txt`](https://github.com/ForestTechMC/ForestRedisAPI/blob/master/LICENSE.txt)をご覧ください。
