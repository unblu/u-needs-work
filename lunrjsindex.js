var documents = [

{
    "id": 0,
    "uri": "documentation/10_user-guide.html",
    "menu": "documentation",
    "title": "User Guide",
    "text": " Table of Contents User guide User guide Suppose you have a setup where u-needs-work is already configured correctly (see Setup ). On any merge request, if a user post a review (or a comment) containing 🔧 (the :wrench: emoji) or needs work then an additional comment will be added to the MR timeline by the u-needs-work tool. Using the New changes since the comment. link, you can access the diff view where you compare the changes since your last review. "
},

{
    "id": 1,
    "uri": "documentation/30_endpoints.html",
    "menu": "documentation",
    "title": "Endpoints",
    "text": " Table of Contents Endpoints Main endpoint Blocking endpoint Replay endpoint Readiness and liveness probes Endpoints Main endpoint POST &lt;server url&gt;/u-needs-work/event This is the principal endpoint, that receive the Comment Webhook event sent by GitLab. Requests are processed asynchronously, meaning that GitLab will receive a 202 Accepted response back immediately. Example: { build_commit : 6af21ad, build_timestamp : 2022-01-01T07:21:58.378413Z, gitlab_event_uuid : 62940263-b495-4f7e-b0e8-578c7307f13d } build_commit and build_timestamp allow you to identify the u-needs-work version. gitlab_event_uuid is the value received in the X-Gitlab-Event-UUID header. Blocking endpoint A secondary endpoint where the process in done in a blocking way is available as well: POST &lt;server url&gt;/u-needs-work/event-blocking With this blocking endpoint you get more information about the created comment in the returned response. Example: { build_commit : 6af21ad, build_timestamp : 2022-01-01T07:21:58.378413Z, gitlab_event_uuid : 62940263-b495-4f7e-b0e8-578c7307f13d, needs_work_note : { project_id : 56, note_id : 42395, note_author_id : 139, note_content : [John Smith](https://gitlab.example.com/jsmith) [requested](https://gitlab.example.com/a_project/-/merge_requests/34#note_1234) changes on this MR.\n\n:eye_in_speech_bubble: [New changes](https://gitlab.example.com/a_project/-/merge_requests/34/diffs?start_sha=e54b4d028af6509e3b97467b153e16753c35747d) since the comment., mr_iid : 34, mr_last_commit_sha : e54b4d028af6509e3b97467b153e16753c35747d, mr_web_url : https://gitlab.example.com/a_project/-/merge_requests/34 }, needs_work_note_type : ADDED } Since GitLab keeps the response obtained when delivering a webhook event and displays it in the webhook admin page, using this endpoint might be interesting for debugging purpose. Replay endpoint An additional endpoint is available to trigger the process using some simplified input compared to comment event body sent by the GitLab Webhook API. POST &lt;server url&gt;/u-needs-work/replay Body: { project_id : 56, note_id : 42333, note_web_url : https://gitlab.example.com/a_project/-/merge_requests/34#note_42333, note_author_id : 37, note_content : This needs work!, mr_iid : 34, mr_last_commit_sha : e54b4d028af6509e3b97467b153e16753c35747d, mr_web_url : https://gitlab.example.com/a_project/-/merge_requests/34, user_name : John Smith, gitlab_event_uuid : test-8902345 } The response is the same as in the blocking case. Using this endpoint is interesting to trigger again the u-needs-work action for a given event using curl , without having to send the complete webhook event body. Readiness and liveness probes The application provides standard probes: &lt;server url&gt;/q/health/live : The application is up and running (liveness). &lt;server url&gt;/q/health/ready : The application is ready to serve requests (readiness). &lt;server url&gt;/q/health : Accumulating all health check procedures in the application. "
},

{
    "id": 2,
    "uri": "documentation/50_build.html",
    "menu": "documentation",
    "title": "Build",
    "text": " Table of Contents Build Running the application locally Packaging the application Build a docker image Run the docker image Build Please refer to the Quarkus documentation for more details. Running the application locally You can run your application in dev mode that enables live coding using: ./gradlew --console=PLAIN quarkusDev This will start the application is dev mode, available on port 8080 . For more details check the Quarkus Gradle Tooling page. Packaging the application The application can be packaged using: ./gradlew build It produces the quarkus-run.jar file in the build/quarkus-app/ directory. Be aware that it’s not an über-jar as the dependencies are copied into the build/quarkus-app/lib/ directory. The application is now runnable using java -jar build/quarkus-app/quarkus-run.jar . If you want to build an über-jar , execute the following command: ./gradlew build -Dquarkus.package.type=uber-jar The application, packaged as an über-jar , is now runnable using java -jar build/u-needs-work-&lt;version&gt;-runner.jar . Build a docker image ./gradlew build \ -Dquarkus.container-image.build=true \ -Dquarkus.container-image.push=true \ -Dquarkus.container-image.registry=&lt;registry name&gt; \ -Dquarkus.container-image.group=&lt;image path&gt; \ -Dquarkus.container-image.name=&lt;image name&gt; \ -Dquarkus.container-image.username=&lt;registry username&gt; \ -Dquarkus.container-image.password=&lt;registry password&gt; Run the docker image docker run -p 8080:8080 -e GITLAB_API_TOKEN=glpat-rXzx1n17cqUnmo437XSf &lt;u-needs-work image name&gt; The server is running on the 8080 port. "
},

{
    "id": 3,
    "uri": "documentation/20_setup.html",
    "menu": "documentation",
    "title": "Setup",
    "text": " Table of Contents Setup GitLab: Generate an API Token Application Setup GitLab: Webhook Setup Dev setup Working with a remote GitLab instance Setup In order to interact with a given Gitlab instance through its REST API, u-needs-work needs to be authorized and authenticated. To do so, together with Gitlab&#8217;s instance URL, an API token must be provided. GitLab: Generate an API Token You will need a personal access token or a group access in order for the tool to interact with your repositories on GitLab. In addition, depending on the project&#8217;s configuration, you might need an extra API token, which can be obtained in the same way as the first one. All the actions are done using the REST API of GitLab. You will need the api scope. Application Setup Some configurations are available for u-needs-work : Example application.properties file gitlab.host=https://gitlab.com gitlab.api.token=glpat-rXzx1n17cqUnmo437XSf You can use any of the Configuration Sources supported by Quarkus to set the values. For example you can use following system property to set the gitlab.api.token value: Setting gitlab.api.token using a system property: export GITLAB_API_TOKEN=glpat-rXzx1n17cqUnmo437XSf GitLab host Specify the location of the GitLab server: key: gitlab.host default value https://gitlab.com GitLab api token Specify the api token value used when u-needs-work is performing REST calls. key: gitlab.api.token No default value. Mandatory for the application to start GitLab: Webhook Setup In the corresponding repository or group configure a Webhook pointing to the location where u-needs-work is available: URL: &lt;server url&gt;/u-needs-work/event Trigger: Comments events Warning From an operational point of view, it might be safer to deploy u-needs-work to a server where only your GitLab instance has access. Read more about the different available endpoints . Dev setup The application can be started locally, check local build section. Working with a remote GitLab instance If you are working locally with a remote gitlab instance (like https://gitlab.com/ ), adding some proxy might be useful: With a tool like ngrok you will get a public url (something like https://2a01-8943-19d-e0a-8b20-645f-f7a2-c2d-9be1.ngrok.io ) that points to your localhost computer. start ngrok (assuming u-needs-work is running locally on port 8080) ngrok http 8080 With a tool like mitmproxy you can proxy the remote instance to capture the REST requests made by u-needs-work to the remote instance: start mitmproxy mitmproxy -p 8888 --mode reverse:https://gitlab.com And then make u-needs-work use localhost:8888 instead of gitlab.com directly: use mitmproxy export GITLAB_HOST=http://localhost:8888 "
},

{
    "id": 4,
    "uri": "search.html",
    "menu": "-",
    "title": "search",
    "text": " Search Results "
},

{
    "id": 5,
    "uri": "lunrjsindex.html",
    "menu": "-",
    "title": "null",
    "text": " will be replaced by the index "
},

];
