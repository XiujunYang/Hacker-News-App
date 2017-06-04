This application refer to Restful api: https://github.com/HackerNews/API
, emulate display as https://news.ycombinator.com/ to show up top story and its comments.

20170604 Modified:
1. Use AsyncTask to replace ThreadPoolExecutor-Handler-Message.
2. Keep Stories and Comments in the same order as returned by the API.
3. Add unit test.