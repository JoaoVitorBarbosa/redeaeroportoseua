from django.http import HttpResponse
from django.template import loader
import json
import requests

def index(request):
	template = loader.get_template('network/index.html')
	return HttpResponse(template.render(request))

def ranking(request):
	template = loader.get_template('network/ranking.html')
	return HttpResponse(template.render(request))	

def graph(request):
	
	args = []
	command = request.GET.get('command', "")
	year = request.GET.get('year', "")
	month = request.GET.get('month', "")
	dayOfMonth = request.GET.get('dayOfMonth', "")
	dayOfWeek = request.GET.get('dayOfWeek', "")
	state = request.GET.get('state', "")
	distance = request.GET.get('distance', "")

	args = [command, year, month, dayOfMonth, dayOfWeek, state, distance]

	config = { 'file': "/home/joao/Desenvolvimento/Spark/Workspace/AirlinesNetwork/target/scala-2.11/airlinesnetwork_2.11-1.0.jar", 
			'className' : "Airlines", 
			'args': args }

	r = requests.post('http://localhost:8998/batches', json=config)
	response_data = r.json()

	return HttpResponse(
            json.dumps(response_data),
            content_type="application/json"
        )

def insert(lista, str):
	if(str != None):
		lista.append(str)

def submit(request):
	command = request.GET.get('command', None)

	config = { 'file': "/home/joao/Desenvolvimento/Spark/Workspace/AirlinesNetwork/target/scala-2.11/airlinesnetwork_2.11-1.0.jar", 
			'className' : "Airlines", 
			#'executorMemory' : '500M',
			'args': [command] }

	r = requests.post('http://localhost:8998/batches', json=config)
	response_data = r.json()

	return HttpResponse(
            json.dumps(response_data),
            content_type="application/json"
        )

def getStatus(request):
	id = request.GET.get('id', None)
    
	r = requests.get('http://localhost:8998/batches/' + id)
	response_data = r.json()

	return HttpResponse(
            json.dumps(response_data),
            content_type="application/json"
        )
