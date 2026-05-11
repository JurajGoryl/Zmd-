MAIN_CLASS = Main
SRC = src

clean:
	@echo "Čistím projekt..."
	rm -rf *.cache/ *.hw/ *.ip_user_files/ *.sim/ *.runs/ .Xil/
	rm -f *.log *.jou *.str *.pb
	rm -rf build/ out/ target/
	rm -f database.txt database.db
	find . -name "*.iml" -type f -delete
	find . -name "*.class" -type f -delete

compile:
	mvn compile

run:
	mvn exec:java -Dexec.mainClass="$(MAIN_CLASS)" -Dexec.args="--enable-native-access=ALL-UNNAMED"
