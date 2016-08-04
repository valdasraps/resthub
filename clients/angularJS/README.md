

index.html

```
<script src="app/services/resthub.js"></script>
```


app.js

```
angular.module('YourAppName')
.constant('ResthubApiUrl', 'http://localhost:2113');
```


controller.js

```
angular.module('YourAppName')
.controller('YourControllerName', function(resthub){

	var options = { type : "application/json2" };

	var sql = "SELECT * FROM namespace.table t WHERE t.column_name = 1234";

	options['sql'] = sql;

	options['ppage'] = 20;  // optional
	options['page'] = 1;	// optional

	resthub.query(options)
		.then(function(response){

			console.log(response);

		});

});

```