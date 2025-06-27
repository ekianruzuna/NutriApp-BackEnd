¡IMPORTANTE! = En los archivos del la rama main se encuentra el NutriApp.postman_collection que es el archivo para descargar e importar con los endpoints en postman para facilitar el testeo de la aplicacion

Link Jira: https://tpfinal1.atlassian.net/jira/software/projects/SCRUM/boards/1?sprints=133%2C100&atlOrigin=eyJpIjoiYmJhYjY2MmNiMGRlNDcwYThkMjY3MjIwMmQ4NmMwYTciLCJwIjoiaiJ9


DOCUMENTACION

1. Introducción
El presente documento contiene la Especificación de Requisitos de Software (ERS) para
el sistema NutriApp.
NutriApp es una aplicación orientada a promover hábitos alimenticios saludables mediante
el registro, seguimiento y análisis de las comidas de los usuarios. El objetivo de esta
especificación es detallar de forma clara y estructurada todos los requisitos funcionales y no
funcionales necesarios para el correcto desarrollo y funcionamiento del sistema.
A lo largo de este documento se describen de manera precisa las funcionalidades
principales, las restricciones del sistema, los requisitos de usuario, así como el
comportamiento esperado del software. Esta especificación está a todo publico con el fin de
asegurar una comprensión común y completa del producto a construir.

1.1 Propósito
El propósito de este documento ERS es proporcionar, del lado de los
programadores, una descripción detallada del programa NutriApp, especificando
todas sus funcionalidades, restricciones y comportamientos esperados. Este
documento servirá como guía técnica durante todo el proceso de desarrollo,
asegurando que el sistema cumpla con los requerimientos establecidos por los
usuarios y stakeholders.
Además, permitirá al equipo de desarrollo contar con una base sólida para el diseño,
la implementación y las pruebas del sistema, promoviendo una visión unificada y
precisa del producto a construir.

1.2 Ámbito del sistema
● El nombre de esta aplicacion fue designado por el equipo de desarrollo
como ‘NutriApp’
● NutriApp permitirá a los usuarios:
- Registrar sus comidas y porciones diarias.
- Visualizar un historial alimenticio.
- Consultar estadísticas personalizadas sobre su alimentación.
- Modificar o eliminar su cuenta si así lo desean.
 NutriApp no incluirá funcionalidades relacionadas con:
- Diagnóstico médico profesional.
- Recomendaciones nutricionales automatizadas avanzadas
basadas en inteligencia artificial.
- Integración con dispositivos externos como pulseras fitness o
apps de terceros en esta primera versión.
● Los principales objetivos del sistema son:
● Promover una alimentación consciente y saludable.
● Brindar una experiencia de usuario intuitiva y accesible.
● Garantizar la privacidad y seguridad de los datos alimenticios de cada
usuario.
● En cuanto a documentos de nivel superior, hasta el momento se cuenta
únicamente con diagramas complementarios que apoyan el diseño y
comprensión del sistema

2. Descripción general del sistema
Narrativa del sistema – Registro nutricional y control calórico personalizado
Se desarrollará un sistema de registro alimenticio y control calórico, orientado
a personas que desean llevar un seguimiento de su nutrición y balance
energético diario. El sistema ofrecerá funcionalidades tanto automáticas
como manuales, para adaptarse a distintos niveles de experiencia del usuario
y diferentes fuentes de información.
- Funcionalidades principales del sistema
+ Gestión de usuarios:
Registro y autenticación de usuarios mediante correo electrónico y
contraseña.
Edición del perfil personal (edad, peso, altura, sexo, nivel de actividad física).
Cálculo del Índice Metabólico Basal (IMB)
El sistema calcula el IMB del usuario en base a sus datos personales.
El usuario selecciona un objetivo:
Bajar de peso
Mantener el peso
Subir de peso
El sistema utiliza esta información para sugerir un plan calórico diario
estimado.
+ Registro de comidas ingeridas:
El usuario puede ingresar el nombre de una comida.
El sistema consulta una API nutricional externa para obtener los valores
nutricionales (calorías, macronutrientes) y guarda esta información en una
base de datos.
El usuario puede marcar comidas como frecuentes o regulares, para facilitar
su selección en futuras cargas.
+ Gestión de comidas no encontradas:
Si una comida no está disponible en la base de datos ni en la API, el usuario
puede enviar una solicitud de alta.
Un administrador revisará estas solicitudes y podrá aprobar, rechazar o editar
los datos antes de agregarlos a la base de datos.
Registro de actividad física y calorías gastadas:
+ El usuario puede:
Ingresar manualmente las calorías gastadas (si las obtuvo de un reloj, una
app externa, etc.).
Especificar el tipo de actividad y su intensidad, y el sistema calculará
automáticamente las calorías estimadas basándose en su IMB, duración de
la actividad y nivel de esfuerzo.
Estas calorías se suman al balance energético diario del usuario.
+ Historial y seguimiento:
Visualización del historial de comidas y calorías consumidas/gastadas por
día.
Seguimiento del progreso respecto al plan calórico.
Gráficos comparativos (consumo vs. objetivo, balance calórico, distribución
de macronutrientes, etc.)

2.1 Perspectiva del producto
NutriApp es una aplicación web desarrollada de manera independiente, pero
integra funcionalidades que dependen del consumo de servicios externos
para ofrecer información nutricional precisa y actualizada a los usuarios.
En particular, el sistema se conecta con la API pública FoodCentral API,
provista por el Departamento de Agricultura de los Estados Unidos (USDA).
Esta API permite acceder a una base de datos extensa de alimentos,
proporcionando información detallada sobre valores nutricionales, porciones y
descripciones de cada ítem alimenticio.
El consumo de esta API externa permite a NutriApp:
● Buscar alimentos por nombre o categoría.
● Obtener datos nutricionales detallados (calorías, macronutrientes,
etc.).
● Mejorar la experiencia del usuario al registrar sus comidas con
información precisa y confiable.
Si bien el sistema principal no depende de otros productos de software
específicos (no requiere integración con sistemas propietarios ni hardware
dedicado), el acceso a FoodCentral API es esencial para el correcto
funcionamiento de las funcionalidades relacionadas con la consulta de
alimentos.

2.2 Objetivos del sistema
NutriApp tiene como objetivo principal brindar a los usuarios una herramienta
integral para el seguimiento y la gestión de sus hábitos alimenticios y de
actividad física, fomentando un estilo de vida más saludable mediante el
acceso a información nutricional confiable y funcionalidades adaptadas a sus
necesidades.
A grandes rasgos, los objetivos del sistema son:
● Facilitar el acceso y personalización del perfil del usuario,
permitiendo a clientes y administradores gestionar su información
personal de manera segura.
● Ofrecer un catálogo completo y accesible de alimentos, con
funcionalidades de búsqueda, filtrado y visualización detallada,
haciendo uso de una API externa (FoodCentral API) para garantizar
datos nutricionales precisos.
● Permitir el registro, visualización y gestión de las comidas
ingeridas, organizadas por tipo (desayuno, almuerzo, merienda,
cena), incluyendo la posibilidad de marcar comidas favoritas y generar
solicitudes para agregar nuevos alimentos al sistema.
● Brindar un historial personalizado de alimentación y actividad
física, que el usuario puede consultar, filtrar y modificar para llevar un
control efectivo de sus progresos.
● Proporcionar funcionalidades para la administración del sistema,
permitiendo a los administradores gestionar usuarios, alimentos,
actividades físicas y solicitudes realizadas por los clientes.
● Automatizar procesos internos clave, como el cálculo del Índice de
Masa Corporal (IMC) del usuario a partir de los datos ingresados, y la
actualización automática del historial nutricional al registrar comidas o
ejercicios.
● Promover la interacción entre cliente y sistema, mediante
notificaciones automáticas al administrador sobre solicitudes
importantes y una interfaz intuitiva que incentive el uso continuo de la
aplicación.

2.3 Herramientas utilizadas
Durante el desarrollo de NutriApp se emplearon diversas herramientas tanto
para la implementación del software como para la gestión del proyecto y la
documentación. A continuación se detallan las principales herramientas
utilizadas:
● Java con Spring Boot: Framework principal para el desarrollo del
backend de la aplicación, facilitando la creación de servicios REST, la
gestión de seguridad, y el acceso a bases de datos mediante JPA.
● MySQL Server: Sistema de gestión de bases de datos relacional
utilizado para almacenar la información persistente del sistema
(usuarios, alimentos, historial, etc.).
● Postman: Herramienta utilizada para probar y validar los endpoints de
la API durante el desarrollo y depuración del backend.
● Git y GitHub: Control de versiones del código fuente y colaboración en
equipo mediante repositorios remotos.
● Jira: Herramienta de gestión de proyectos usada para organizar
tareas, seguimiento de incidencias y control de avances en el
desarrollo.
● Discord: Plataforma de comunicación utilizada por el equipo para
coordinarse, debatir decisiones técnicas y compartir avances.
● Draw.io: Aplicación web utilizada para la creación de los diagramas
UML (casos de uso, clases, arquitectura, etc.) que forman parte de la
documentación técnica del sistema.
● FoodData Central: Api externa que nos permite acceder a una
base de datos de alimentos con todo su valor nutricional .

3. Definición de requisitos del sistema
3.1 Requisitos funcionales
RF01: El sistema debe permitir al cliente y al admin iniciar sesión
RF02: El sistema debe permitir al cliente registrarse
RF03: El sistema debe permitir al cliente y admin ver su perfil
RF04: El sistema debe permitir al cliente y admin modificar su perfil
RF05: El sistema debe permitir al cliente eliminar su cuenta
RF06: El sistema debe permitir al cliente y admin listar alimentos
RF07: El sistema debe permitir al cliente y admin filtrar alimentos
RF08: El sistema debe permitir al admin ABM alimentos
RF09: El sistema debe permitir al cliente y admin ver info completa de
alimento
RF10: El sistema debe permitir al cliente ABM de comida ingerida para un
tiempo de comida específico (desayuno, almuerzo, merienda, cena)
RF11: El sistema debe permitir al cliente listar historial de comida ingerida
RF12: El sistema debe permitir al cliente filtrar historial de comida ingerida
RF13: El sistema debe permitir al cliente ABM de comidas favoritas
RF14: El sistema debe permitir al cliente listar comidas favoritas
RF15: El sistema debe permitir al cliente filtrar comidas favoritas
RF16: El sistema debe permitir al cliente ABM de solicitud de alta de comidas
RF17: El sistema debe permitir al cliente y admin listar solicitudes de alta de
comidas
RF18: El sistema debe permitir al cliente y admin filtrar solicitudes de alta de
comidas
RF19: El sistema debe permitir al admin rechazar una solicitud de alta de
comida
RF20: El sistema debe permitir al admin aceptar una solicitud de alta de
comidas
RF21: El sistema debe permitir al admin listar clientes
RF22: El sistema debe permitir al admin filtrar clientes
RF23: El sistema debe permitir al admin la BAJA de clientes
RF24: El sistema debe permitir al cliente visualizar su Índice Metabólico
Basal (IMB) en su perfil, una vez que se hayan ingresado todos los datos
necesarios.
RF25: El sistema debe permitir al cliente y al admin listar actividades físicas
RF26: El sistema debe permitir al cliente y al admin filtrar actividades físicas
RF27: El sistema debe permitir al cliente realizar ABM de actividades físicas
realizada
RF28: El sistema debe permitir al cliente listar historial de actividades físicas
realizadas
RF29: El sistema debe permitir al cliente filtrar actividades físicas realizadas
RF30: El sistema debe permitir al admin realizar ABM de actividades físicas
RF32: El sistema debe permitir al cliente visualizar el historial del plan
nutricional.
RF33: El sistema debe permitir al cliente modificar el historial del plan
nutricional.
RF34: El sistema debe permitir al cliente filtrar el historial del plan nutricional
por día.

3.2 Requisitos no funcionales
.RNF01 - Rendimiento:
El sistema debe garantizar tiempos de respuesta menores a 2 segundos para
operaciones comunes como inicio de sesión, carga de perfiles, listado de alimentos,
etc.
RNF02 - Disponibilidad:
El sistema debe estar disponible al menos el 99.5% del tiempo mensual,
exceptuando ventanas de mantenimiento planificadas.
RNF03 - Escalabilidad:
El sistema debe ser escalable horizontalmente para soportar un crecimiento en el
número de usuarios y transacciones sin pérdida significativa de rendimiento.
RNF04 - Seguridad:
● El sistema debe encriptar contraseñas mediante algoritmos robustos (por
ejemplo, bcrypt).
● Toda la comunicación entre cliente y servidor debe realizarse mediante
HTTPS.
● Debe implementarse control de acceso con roles diferenciados para clientes
y administradores.
RNF05 - Usabilidad:
El sistema debe contar con una interfaz amigable e intuitiva, diseñada bajo
principios de usabilidad (por ejemplo, accesibilidad, navegación clara,
retroalimentación visual).
RNF06 - Mantenibilidad:
El código fuente debe seguir principios de diseño limpio (clean code) y buenas
prácticas para facilitar su mantenimiento y evolución.
RNF07 - Auditabilidad:
El sistema debe generar logs de auditoría para operaciones críticas (como
eliminación de cuentas, aceptación/rechazo de solicitudes, ABM de alimentos).
.RNF08 - API
El sistema debe permitir interactuar con una API de alimentos para permitirnos
extraer los valores nutricionales de dichos alimentos.
RNF09 - API
El sistema debe permitir interactuar con una API que nos permita notificar al
administrador sobre las solicitudes de alta de alimentos.
RNF10: Base de datos
El sistema debe permitir persistencia de datos mediante una base de datos que
permita almacenar usuarios, comidas, etc.
- Funcionalidad:
RNF11: El sistema debe funcionar con 4 administradores precargados
RNF12: El sistema debe notificar al admin sobre solicitudes de alta alimentaria
mediante mail y/o wpp.
RNF13: El sistema debe registrar automáticamente en el historial del plan
nutricional toda comida ingerida o actividad física realizada por el cliente al momento
de ser cargada en el sistema.
RNF14: El sistema debe calcular automáticamente el Índice Metabólico Basal (IMB)
del cliente al guardar los datos personales (edad, peso, altura, sexo y nivel de
actividad física), utilizando una fórmula estándar.
