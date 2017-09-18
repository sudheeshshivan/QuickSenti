	var w = 350;
	var h = 250;
	var r = h/2;

	// var data = [
	// 	{
	// 		"sentiment":"Very Positive", "value":12,
	// 		"data" : [
	// 			{
	// 				"date" : "12-05-2014",
	// 				"value" : 10
	// 			},
	// 			{
	// 				"date" : "14-05-2014",
	// 				"value" : 20
	// 			},
	// 			{
	// 				"date" : "15-05-2014",
	// 				"value" : 30
	// 			},
	// 			{
	// 				"date" : "17-05-2014",
	// 				"value" : 40
	// 			}
	// 		]
	// 	}
	// ];
	var data;

	var arc = d3.svg.arc()
		.outerRadius(r)
		.innerRadius(20);

function drawSign(){

	d3.select("#piechart").append("circle")
    	.attr("r", 20)      
	  	.attr("transform", "translate(" + r + "," + r + ")")
    	.style("fill","#FFF")
    	.on("click", zoomOut);

	var negativeSign = d3.select("#piechart").append("g");
	var positiveSign = d3.select("#piechart").append("g");
	var veryNegativeSign = d3.select("#piechart").append("g");
	var veryPositiveSign = d3.select("#piechart").append("g");
	var neutralSign = d3.select("#piechart").append("g");

		/*Negative Sign Manipulation*/
    	negativeSign.append("rect").attr("width", 10)
    	.attr("height", 10)
    	.style("fill","#8C0000");
    	negativeSign
    	.append("text")
    	.text("Negative")
	  	.attr("transform", "translate(12,10)");

	  	/*Positive Sign Manipulation*/
    	positiveSign.append("rect").attr("width", 10)
    	.attr("height", 10)
    	.style("fill","#006B24");
    	positiveSign
    	.append("text")
    	.text("Positive")
	  	.attr("transform", "translate(12,10)");

	  	/*Very Negative Sign Manipulation*/
    	veryNegativeSign.append("rect").attr("width", 10)
    	.attr("height", 10)
    	.style("fill","#520000");
    	veryNegativeSign
    	.append("text")
    	.text("Very Negative")
	  	.attr("transform", "translate(12,10)");

	  	/*Very Positive Sign Manipulation*/
    	veryPositiveSign.append("rect").attr("width", 10)
    	.attr("height", 10)
    	.style("fill","#004C1A");
    	veryPositiveSign
    	.append("text")
    	.text("Very Positive")
	  	.attr("transform", "translate(12,10)");

	  	/*Nuetral Sign Manipulation*/
    	neutralSign.append("rect").attr("width", 10)
    	.attr("height", 10)
    	.style("fill","#B24700");
    	neutralSign
    	.append("text")
    	.text("Nuetral")
	  	.attr("transform", "translate(12,10)");


	  	/*Arrange The signs into bottom*/
	  	negativeSign.attr("transform", "translate(270,180)");
	  	positiveSign.attr("transform", "translate(270,192)");
	  	veryNegativeSign.attr("transform", "translate(270,204)");
	  	veryPositiveSign.attr("transform", "translate(270,216)");
	  	neutralSign.attr("transform", "translate(270,228)");
	}


function drawPieChart(dataObj){
	data = dataObj;
	drawParent();
	drawSign();
}
    function zoomOut(){
    	// console.log(data);
    	changeData(data);
    }
    function changeData(d){
    	// console.log(d);
    	if(typeof d.data == 'undefined' ){
    		drawParent();
    	}
    	else if(typeof d.data.date == 'undefined' ){
    		drawChild(d);
    	}
    	else{
    		drawParent();
    	}
    }

    function drawParent(){
		vis = d3.select('#piechart')
		.data([data])	
		.attr("width", w)
		.attr("height", h)
		.append("g")
		.attr("transform", "translate(" + r + "," + r + ")");
		pie = d3.layout.pie()
    			.value(function(d){return d.value;});
		// select paths, use arc generator to draw
		arcs = vis.selectAll("g.slice").data(pie).enter().append("svg:g").attr("class", "slice");
		arcs.append("svg:path")
	    .attr("fill", function(d, i){
	        if(d.data.sentiment=="Negative"){ return "#8C0000"; }
	        else if(d.data.sentiment=="Very negative"){ return "#520000"; }
	        else if(d.data.sentiment=="Very positive"){ return "#004C1A"; }
	        else if(d.data.sentiment=="Positive"){ return "#006B24"; }
	        else{ return "#B24700"; }
	        // alert(d.data.sentiment);
	    })	    
		.on("click", changeData)
	    .transition().duration(500)
	    .attrTween('d', function(d) {
		   var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
		   return function(t) {
		       d.endAngle = i(t);
		     return arc(d);
		   }
		});
	    // .attr("d", function (d) {
	    //     // log the result of the arc generator to show how cool it is :)
	    //     return arc(d);
	    // });

    }

    function drawChild(d){

		// var color = d3.scale.category20c();
		console.log(d.data.sentiment);
		var color = [];
		switch(d.data.sentiment){
			case 'Positive':
				color[0] = "#006B24";
				color[1] = "#007F20"
			break;			
			case 'Very positive':
				color[0] = "#004C1A";
				color[1] = "#005C1A"
			break;
			case 'Negative':
				color[0] = "#6A0000";
				color[1] = "#7A0000"
			break;			
			case 'Very negative':
				color[0] = "#520000";
				color[1] = "#5F0000"
			break;
			default:
				color[0] = "#B54700";
				color[1] = "#B53700";
			break;
		}	
		vis = d3.select('#piechart')
		.data([d.data.data])	
		.attr("width", w)
		.attr("height", h)
		.append("g")
		.attr("transform", "translate(" + r + "," + r + ")");
		pie = d3.layout.pie().value(function(d){return d.value;});
		// select paths, use arc generator to draw
		arcs = vis.selectAll("g.slice").data(pie).enter().append("svg:g").attr("class", "slice");
		var childArc = arcs.append("path")

		.on("click", changeData);

		childArc.attr("fill", function(d, i){
	        return color[i%2];
	    })	    	    
	    .transition().duration(500)
	    .attrTween('d', function(d) {
		   var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
		   return function(t) {
		       d.endAngle = i(t);
		     return arc(d);
		   }
		});
	    // .attr("d", function (d) {
	    //     // log the result of the arc generator to show how cool it is :)
	    //     // console.log(arc(d));
	    //     return arc(d);
	    // })   ;
    }