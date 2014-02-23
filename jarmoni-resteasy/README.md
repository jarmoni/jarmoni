### Example Json-representation

    {
	"links":
		[
			{
				"rel":"self","href":"http://localhost:8080/items/get"
			},
			{
				"rel":"next","href":"http://localhost:8080/items/next"
			}
		]
	,
	"items":
		[
			{
				"links":
					[
						{"rel":"self","href":"http://localhost:8080/items/get/john"}
					],
				"name":"john","age":25
			},
			{
				"links":
					[
						{"rel":"self","href":"http://localhost:8080/items/get/jane"}
					],
				"name":"doe","age":30
			}
		]
    }
    
### Usage
See tests in common- and spring-module.
    