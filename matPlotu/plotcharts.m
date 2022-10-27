clear

N1data=load("../N1/N1out.mat");
N2data=load("../N2/N2out.mat");
Wdata=load("../Workload/roi_profile.mat");

Wdata.roi=[300,Wdata.roi(1,1:end-1)];
rates=[];
for i=1:size(Wdata.roi,2)
    if(i==1)
        rates=[rates,repmat(Wdata.roi(i),1,sum(N1data.ctime<Wdata.ctime(1,i)))];
    else
        rates=[rates,repmat(Wdata.roi(i),1,sum(N1data.ctime>Wdata.ctime(1,i-1) ...
                          & N1data.ctime<Wdata.ctime(1,i)) )];
    end
end

startTime=0;

n1d=N1data.rt;
n2d=N2data.rt;

N1req=0.25;
N2req=0.15;

n1Cum=cumsum(n1d)./linspace(1,size(n1d,2),size(n1d,2));
n2Cum=cumsum(n2d)./linspace(1,size(n2d,2),size(n2d,2));

% figure
% hold on
% title("N1_rt")
% plot(n1Cum);
% stairs(smoothdata(n1d,"movmean"));
% yline(N1req,'-.');
% grid on;
% box on;
% e1=abs(n1Cum(1,end)-N1req)*100/N1req;

% figure
% hold on
% title("N2")
% stairs(smoothdata(n2d,"movmean"));
% plot(n2Cum);
% yline(N2req,'-.');
% grid on;
% box on;
% e2=abs(n2Cum(1,end)-N2req)*100/N2req;
% 
% figure
% hold on
% title("Core")
% stairs(N1data.core);
% stairs(N2data.core);
% grid on;
% box on;
% legend("N1","N2")
% 
% figure
% hold on
% title("u")
% stairs(N1data.u);
% stairs(N2data.u);
% grid on;
% box on;
% legend("N1","N2")
% 
% figure
% hold on
% title("roi")
% stairs(rates);
% grid on;
% box on;

% figure('units','normalized','outerposition',[0 0 1 1])
% subplot(7,1,1);
% stairs((N1data.ctime-N1data.ctime(1))/(10^9),rates);
% ylim([min(rates),max(rates)*1.05])
% title('(a) input rate [req/s]') 
% grid on;
% box on;

subplot(7,1,2);
hold on
stairs((N1data.ctime-N1data.ctime(1))/(10^9),n2d+n1d);
plot((N1data.ctime-N1data.ctime(1))/(10^9),n1Cum+n2Cum,"LineWidth",1.5)
yline(N2req+N1req,'-.',"LineWidth",1.5);
title('(b) overall response time [s]') 
legend("istantaneous","average","setpoint","Orientation","horizontal")
grid on;
box on;

subplot(7,1,3);
hold on
stairs((N1data.ctime-N1data.ctime(1))/(10^9),smoothdata(n2d+n1d,"movmean",4));
plot((N1data.ctime-N1data.ctime(1))/(10^9),n1Cum+n2Cum,"LineWidth",1.5)
yline(N2req+N1req,'-.',"LineWidth",1.5);
title('(c) overall response time, 5 seconds average [s]') 
legend("istantaneous","average","setpoint","Orientation","horizontal")
grid on;
box on;

subplot(7,1,4);
hold on
stairs((N1data.ctime-N1data.ctime(1))/(10^9),n1d);
plot((N1data.ctime-N1data.ctime(1))/(10^9),n1Cum,"LineWidth",1.5)
yline(N1req,'-.',"LineWidth",1.5);
stairs((N2data.ctime-N2data.ctime(1))/(10^9),n2d);
plot((N2data.ctime-N2data.ctime(1))/(10^9),n2Cum,"LineWidth",1.5)
yline(N2req,'-.',"LineWidth",1.5);
title('(d) tier response time [s]') 
legend("N1 istantaneous","N1 average","N1 setpoint","N2 istantaneous","N2 average","N2 setpoint","Orientation","horizontal")
grid on;
box on;

subplot(7,1,5);
hold on
plot((N1data.ctime-N1data.ctime(1))/(10^9),N1data.u,"LineWidth",1.5);
plot((N1data.ctime-N1data.ctime(1))/(10^9),N2data.u,"LineWidth",1.5,"LineStyle",'-.')
title('(e)  linearised control signals') 
legend("N1","N2","Orientation","horizontal","Location","southeast")
grid on;
box on;

subplot(7,1,6);
hold on
plot((N1data.ctime-N1data.ctime(1))/(10^9),N1data.core,"LineWidth",1.5);
plot((N1data.ctime-N1data.ctime(1))/(10^9),N2data.core,"LineWidth",1.5,"LineStyle",'-.')
title('(f)  allotted resources [cores]') 
legend("N1","N2","Orientation","horizontal")
grid on;
box on;

subplot(7,1,7);
hold on
plot((N1data.ctime-N1data.ctime(1))/(10^9),[0,diff(N1data.ctime/10^9)],"LineWidth",1.5);
plot((N1data.ctime-N1data.ctime(1))/(10^9),[0,diff(N2data.ctime/10^9)],"LineWidth",1.5,"LineStyle",'-.')
title('(g)  time between control actions [s]') 
legend("N1","N2","Orientation","horizontal")
grid on;
box on;
xlabel("time (s)")

% set(gcf,'color','w');
% exportgraphics(gcf,'realsim1.pdf')
% close()
