$(document).ready(function(){

	$("#li-index").addClass("active");
	
	$("#state").empty();
	
	$("#state").append("<option>ALL</option>");

	$.each( siglaState, function( key, value ) {
		$("#state").append("<option>" + key + "</option>");
	});


	$("#get-graph").click(function(){
		var $btn = $(this).button('loading')

		var year = $("#year").val();
		var month = $("#month").val();
		var dayOfMonth = $("#dayOfMonth").val();
		var dayOfWeek = $("#dayOfWeek").val();
		var state = $("#state").val();
		var distance = $("#distance").val();

		$.get('graph?command=triplets&year=' + year	+ "&month=" + month + "&dayOfMonth=" + dayOfMonth + "&dayOfWeek=" + dayOfWeek + "&state=" + state + "&distance=" + distance, function(data){ 
			console.info(data);
			getStatus(data.id, $btn);
		})
	})

	$("#max-degree").click(function(){
		$.get('submit?command=maxDegree', function(data){ console.log(data) })
	});

	$("#get-status").click(function(){
		var id = $("#id-session").val();
		$.get('getStatus?id=' + id, function(data){ console.log(data) })
	});

	//getJsonGraph();
	//drawCanvas();
});

var getJsonGraph = function(){
	$.getJSON('http://localhost:8000/static/network/static/network/output/graph.json', function(triplets){ 
		console.info(triplets);
		drawCanvas(triplets);
	});
}

var drawVertex = function(ctx, x, y)
{
	var aux = changeCoord(x, y);
	x = aux[0];
	y = aux[1];

	ctx.beginPath();
    ctx.arc(x,y,2,0,Math.PI*2,true); 
    ctx.fill();
}

var drawEdge = function(ctx, x1, y1, x2, y2)
{
	var aux1 = changeCoord(x1, y1);
	x1 = aux1[0];
	y1 = aux1[1];

	var aux2 = changeCoord(x2, y2);
	x2 = aux2[0];
	y2 = aux2[1];

	ctx.lineWidth = 0.1;
	ctx.beginPath();
 	ctx.moveTo(x1, y1);
 	ctx.lineTo(x2, y2);
 	ctx.strokeStyle = "#09F"
   	ctx.stroke();
}

var drawCanvas = function(triplets){
	var canvas = document.getElementById('graph-canvas');

	xmax = canvas.width;
	ymax = canvas.height;


	if (canvas.getContext){
		var ctx = canvas.getContext('2d');

		ctx.clearRect(0, 0, canvas.width, canvas.height);

    	for(var i = 0; i < triplets.length; i++)
    	{
    		drawVertex(ctx, parseFloat(triplets[i].src.latitude), parseFloat(triplets[i].src.longitude));
    		drawVertex(ctx, parseFloat(triplets[i].dest.latitude), parseFloat(triplets[i].dest.longitude));
    		drawEdge(ctx, parseFloat(triplets[i].src.latitude), parseFloat(triplets[i].src.longitude), parseFloat(triplets[i].dest.latitude), parseFloat(triplets[i].dest.longitude));
    	}

	} 	

}

var changeCoord = function(lat, long){

	// latitude is y
	// longitude is x
	
	var canvas = document.getElementById('graph-canvas');
	widthCanvas = canvas.width;
	heightCanvas = canvas.height;

	xtop = -128.1780;
	xbottom = -64.4999
	ytop = 50.6381;
	ybottom = 22.6597;
	
	larguraMapa = (xtop - xbottom) * -1;
	alturaMapa =  ytop - ybottom;
	

	xnovo = (Math.abs(long - xtop)/larguraMapa)*widthCanvas
	ynovo = (Math.abs(lat - ytop)/alturaMapa)*heightCanvas

	return [xnovo, ynovo];
}

var getStatus = function(id, btn)
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
				getJsonGraph();
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

var siglaState = {
    "AL": "Alabama",
    "AK": "Alaska",
    "AS": "American Samoa",
    "AZ": "Arizona",
    "AR": "Arkansas",
    "CA": "California",
    "CO": "Colorado",
    "CT": "Connecticut",
    "DE": "Delaware",
    "DC": "District Of Columbia",
    "FM": "Federated States Of Micronesia",
    "FL": "Florida",
    "GA": "Georgia",
    "GU": "Guam",
    "HI": "Hawaii",
    "ID": "Idaho",
    "IL": "Illinois",
    "IN": "Indiana",
    "IA": "Iowa",
    "KS": "Kansas",
    "KY": "Kentucky",
    "LA": "Louisiana",
    "ME": "Maine",
    "MH": "Marshall Islands",
    "MD": "Maryland",
    "MA": "Massachusetts",
    "MI": "Michigan",
    "MN": "Minnesota",
    "MS": "Mississippi",
    "MO": "Missouri",
    "MT": "Montana",
    "NE": "Nebraska",
    "NV": "Nevada",
    "NH": "New Hampshire",
    "NJ": "New Jersey",
    "NM": "New Mexico",
    "NY": "New York",
    "NC": "North Carolina",
    "ND": "North Dakota",
    "MP": "Northern Mariana Islands",
    "OH": "Ohio",
    "OK": "Oklahoma",
    "OR": "Oregon",
    "PW": "Palau",
    "PA": "Pennsylvania",
    "PR": "Puerto Rico",
    "RI": "Rhode Island",
    "SC": "South Carolina",
    "SD": "South Dakota",
    "TN": "Tennessee",
    "TX": "Texas",
    "UT": "Utah",
    "VT": "Vermont",
    "VI": "Virgin Islands",
    "VA": "Virginia",
    "WA": "Washington",
    "WV": "West Virginia",
    "WI": "Wisconsin",
    "WY": "Wyoming"
}