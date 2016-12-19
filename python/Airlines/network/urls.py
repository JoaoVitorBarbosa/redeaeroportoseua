from django.conf.urls import url 

from . import views

urlpatterns = [
	url(r'^$', views.index, name='index'),
	url(r'^ranking$', views.ranking, name='ranking'),
	url(r'^graph$', views.graph, name='graph'),
	url(r'^submit$', views.submit, name='submit'),
	url(r'^getStatus$', views.getStatus, name='getStatus'),
]