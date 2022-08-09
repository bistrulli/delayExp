#!/bin/bash

gcloud container clusters get-credentials cluster-1 --zone northamerica-northeast1-a --project my-project-1509758122771

kubectl apply -f ./pod/deploy_db.yaml
kubectl apply -f ./pod/deploy_ctrl.yaml
kubectl apply -f ./pod/deploy_sys.yaml
kubectl apply -f ./services/monitor.yaml
kubectl apply -f ./services/monitor-ext.yaml