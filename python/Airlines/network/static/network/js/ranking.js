$(document).ready(function(){

	$("#li-ranking").addClass("active");

	$("#get-pagerank").click(function(){
		var $btn = $(this).button('loading')

		$.get('submit?command=pagerank', function(data){ 
			console.log(data) 
			getStatus(data.id, $btn, getJsonPG);
		})

	});

	$("#get-degree").click(function(){
		var $btn = $(this).button('loading')

		$.get('submit?command=degree', function(data){ 
			console.log(data) 
			getStatus(data.id, $btn, getDegree);
		})

	});

	$("#get-indegree").click(function(){
		var $btn = $(this).button('loading')

		$.get('submit?command=inDegree', function(data){ 
			console.log(data) 
			getStatus(data.id, $btn, getInDegree);
		})

	});

	$("#get-outdegree").click(function(){
		var $btn = $(this).button('loading')

		$.get('submit?command=outDegree', function(data){ 
			console.log(data) 
			getStatus(data.id, $btn, getOutDegree);
		})

	});
	
	$("#get-triangle").click(function(){
		var $btn = $(this).button('loading')

		$.get('submit?command=triangle', function(data){ 
			console.log(data) 
			getStatus(data.id, $btn, getTriangle);
		})

	});

});

var getTriangle = function(){
	$.getJSON('http://localhost:8000/static/network/static/network/output/triangle.json', function(triangle){ 
		console.info(triangle);
		buildTable("Triangle", triangle);
	});
}


var getDegree = function(){
	$.getJSON('http://localhost:8000/static/network/static/network/output/degree.json', function(degree){ 
		console.info(degree);
		buildTable("Degree", degree);
	});
}

var getInDegree = function(){
	$.getJSON('http://localhost:8000/static/network/static/network/output/indegree.json', function(indegree){ 
		console.info(indegree);
		buildTable("InDegree", indegree);
	});
}

var getOutDegree = function(){
	$.getJSON('http://localhost:8000/static/network/static/network/output/outdegree.json', function(outdegree){ 
		console.info(outdegree);
		buildTable("OutDegree", outdegree);
	});
}


var getJsonPG = function(){
	$.getJSON('http://localhost:8000/static/network/static/network/output/pagerank.json', function(pagerank){ 
		console.info(pagerank);
		buildTable("PageRank", pagerank);
	});
}

var buildTable = function(title, data)
{
    $("#divTable").empty();
    $("#divTable").append("<table class='table table-bordered'>")

    $("#divTable table").append("<thead><tr><th>#</th> <th>" + title + "</th><th>Airport</th></tr></thead>");
    $("#divTable table").append("<tbody>");

    for(var i = 0; i < data.length; i++)
    {
        $("#divTable table").append("<tr><th scope=row>" + (i + 1) + "</th> <td>" + data[i].value + "</td> <td>" + data[i].airport.airport + "</td></tr>");
    }

    $("#divTable table").append("</tbody>");
    $("#divTable").append("</table>")

}

var getStatus = function(id, btn, callback)
{
	console.info('getting status of session ' + id);
	var idSession = id;

	var interval = setInterval(function(){
		$.get('getStatus?id=' + idSession, function(data){ 
			if(data.state == "success")
			{
				console.info(data);
				clearInterval(interval);
				btn.button('reset');
				callback();
			}
			if(data.state == "error")
			{
				console.error(data);
				clearInterval(interval);
				btn.button('reset')
			}

			console.log(data)
		});
	}, 1000);
	
}