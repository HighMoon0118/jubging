<template>
  <div>
    <div id="chart"></div>
  </div>
</template>

<script src="https://d3js.org/queue.v1.min.js"></script>

<script>
import { HTTP } from '@/util/http-common';

export default {
  name: 'RegionRank',
  data() {
    return {
    }
  },
  mounted() {
    var width = 380,
        height = 600;

    var svg = d3.select("#chart").append("svg")
        .attr("width", width)
        .attr("height", height);

    var projection = d3.geo.mercator()
        .center([127.8, 36])
        .scale(5000)
        .translate([width/2, height/2]);

    var path = d3.geo.path()
        .projection(projection);

    // 0 60, 120, 180, 270, 360, 450, 540
    var quantize = d3.scale.quantize()
        .domain([0, 100])
        .range(d3.range(9).map(function(i) { return "p" + i; }));

    var popByName = d3.map();

    queue()
      .defer(d3.json, "municipalities-topo-simple.json")
      .defer(data_request)
      .await(ready);
  

    function data_request(callback) {
      HTTP.get(`/map`)
      .then(res => {
        return res.data.data
      })
      .then(datas => {
        datas.forEach((data) => {
          popByName.set(data.code, +data.count)
        })
        callback()
      })
    }

    function ready(error, data) {
      var features = topojson.feature(data, data.objects["municipalities-geo"]).features;

      features.forEach(function(d) {
        d.properties.jubgingCnt = popByName.get(d.properties.code);
        d.properties.density = d.properties.jubgingCnt;
        d.properties.quantized = quantize(d.properties.density);
      });

      svg.selectAll("path")
        .data(features)
        .enter().append("path")
        .attr("class", function(d) { return "municipality " + d.properties.quantized; })
        .attr("d", path)
        .attr("id", function(d) { return d.properties.name; })
        .append("title")
        .text(function(d) {
          return d.properties.name + " " + d.properties.jubgingCnt + "줍깅" 
        });
    }
  }
}
</script>

<style> 
  svg .municipality { fill: red; }
  svg .municipality:hover { stroke: #333; }
  svg .municipality.p0 { fill: rgb(241, 244, 247); }
  svg .municipality.p1 { fill: rgb(222,235,247); }
  svg .municipality.p2 { fill: rgb(198,219,239); }
  svg .municipality.p3 { fill: rgb(158,202,225); }
  svg .municipality.p4 { fill: rgb(107,174,214); }
  svg .municipality.p5 { fill: rgb(66,146,198); }
  svg .municipality.p6 { fill: rgb(33,113,181); }
  svg .municipality.p7 { fill: rgb(8,81,156); }
  svg .municipality.p8 { fill: rgb(8,48,107); }
  svg text { font-size: 10px; }
</style>