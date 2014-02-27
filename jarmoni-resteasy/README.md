### Example Json-representation

    {
       "version":"1.0.0",
	   "links":
		[
			{
				"rel":"self",
				"href":"http://localhost:8080/items/get"
			},
			{
				"rel":"next",
				"href":"http://localhost:8080/items/next"
			}
		]
	   ,
	   "items":
		[
			{
				"links":
				[
					{
					   "rel":"self",
					   "href":"http://localhost:8080/items/get/john"
					}
				],
				"data":
			    {
			        "name":"john",
			        "age":25
			    }
			},
			{
				"links":
				[
					{
					   "rel":"self",
					   "href":"http://localhost:8080/items/get/jane"
					}
				],
				"data":
				{
				    "name":"jane",
				    "age":30
				}
			}
		]
    }
    
### Usage
See tests in common- and spring-module.
    