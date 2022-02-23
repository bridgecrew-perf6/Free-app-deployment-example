# Free app deployment example
### An example how to deploy your application for free

Tool we need:
- [Telegram app](https://telegram.org/) as UI
- [Cloud Functions](https://console.cloud.google.com/functions) as a server
- [Cloud FireStore](https://console.cloud.google.com/firestore) as a database

Steps:
1. Create new bot in [BotFather](https://t.me/BotFather) using `/newbot` command (we will need `bot token` further)
2. Enable [Functions](https://console.cloud.google.com/functions) API and create a `Cloud Function` instance using code in this repo
3. Recommended function configuration:
    1. Environment: 2nd gen
    2. Authentication:  Allow unauthenticated invocations 
    3. Memory allocated: 256 MiB
    4. Timeout: 30 seconds
    5. Minimum number of instances: 0 (1 if you want to avoid cold start)
    6. Runtime environment variables: `PROJECT_ID` from [settings](https://console.cloud.google.com/iam-admin/settings) and `BOT_TOKEN` from [BotFather](https://t.me/BotFather)
4. Enable [FireStore](https://console.cloud.google.com/firestore) API, no need to create a collection.
5. Set `WebHook` using terminal: `curl --data "url=`${CLOUD_FUNCTION_URL}`&allowed_updates=["message","callback_query"]" https://api.telegram.org/bot`BOT_TOKEN`/setWebhook`
6. Click `start` in your created bot
7. Enjoy 🙂

