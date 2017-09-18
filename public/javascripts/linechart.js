function drawLineChart(data){
  var margin = {top: 20, right: 20, bottom: 30, left: 50},
    width = 400 - margin.left - margin.right,
    height = 250 - margin.top - margin.bottom;


  var x = d3.scale.ordinal()
    .rangeRoundBands([0, width], 0);

  var y = d3.scale.linear()
    .range([height, 0]);

  var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

  var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left");

  var line = d3.svg.line()
    .x(function(d) { return x(d.name); })
    .y(function(d) { return y(d.value); })
    .interpolate("basis");

  var svg = d3.select/*("#linegraph").append*/("#linechart")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


  data.forEach(function(d) {
    d.value = +d.value;
  });

  x.domain(data.map(function(d) { return d.name; }));
  y.domain(d3.extent(data, function(d) { return d.value; }));

  svg.append("g")
    .attr("class", "x axis")    
    .transition().duration(2000)
    .attr("transform", "translate(0," + height + ")")
    .call(xAxis);

  svg.append("g")
    .attr("class", "y axis")
    .call(yAxis)
    .append("text")
    .attr("transform", "rotate(-90)")
    .attr("y", 6)
    .attr("dy", ".71em")
    .style("text-anchor", "end")
    .text("Positive");

  svg.append("path")
    .datum(data)
    .attr("class", "line")
    .attr("transform", "translate("+ (width / data.length) / 2 +",0)")
    .attr("d", line);

}