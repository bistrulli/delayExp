clear

N1data=load("../N1/N1out.mat");
N2data=load("../N2/N2out.mat");
%Wdata=load("../Workload/roi_profile.mat");

n1d=N1data.rt(1:1:end);
n2d=N2data.rt(1:1:end);

N1req=0.20;
N2req=0.15;

n1Cum=cumsum(n1d)./linspace(1,size(n1d,2),size(n1d,2));
n2Cum=cumsum(n2d)./linspace(1,size(n2d,2),size(n2d,2));

figure
hold on
title("N1")
stairs(n1d);
plot(n1Cum);
yline(N1req,'-.');

e1=abs(n1Cum(1,end)-N1req)*100/N1req;



figure
hold on
title("N2")
stairs(n2d);
plot(n2Cum);
yline(N2req,'-.');
e2=abs(n2Cum(1,end)-N2req)*100/N2req;
