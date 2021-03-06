# @Queryable API Covention

<img src="docs/queryable.png"><br/>

# Simple JPA Entiity

The query parameters to generate query on databse.

```
public class Student extends PanacheEntityBase {

    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    @Id
    public String uuid;
    public String name;
    public String surname;
    public String tags;
    // ie: IT, FR, DE
    public String nationality;
    public Integer age;
    public String school_uuid;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Rome")
    public LocalDate birthday_date;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Rome")
    public LocalDate last_examen_date;
    
    public boolean active;
}
```
## Some query parameters examples:

## like.xxxx
- GET: /api/v1/users?like.surname=Pizz
- QUERY CONDITION: select * from users where lowercase(surname) = :surname 
- PARAMETER: surname= '%'+lowercase(like.surname) +'%'

## obj.xxxx
- GET: /api/v1/users?obj.nationality=IT
- QUERY CONDITION: select * from users where nationality = :nationality 
- PARAMETER: nationality = obj.nationality

## obj for list
- GET: /api/v1/users?obj.nationalities=IT,FR,DE
- QUERY CONDITION: select * from users where nationality IN (:nationality_list)
- PARAMETER: nationality_list = [ 'IT','FR','DE' ]

## nil.xxx
- GET: /api/v1/users?nil.last_examen_date=xxxxx
- QUERY CONDITION: select * from users where last_examen_date IS NOT NULL

## null.xxx
- GET: /api/v1/users?null.last_examen_date=2021-01-31
- QUERY CONDITION: select * from users where last_examen_date IS NULL

## from.xxx && to.xxx
- GET: /api/v1/users?from.last_examen_date=2021-01-01&to.last_examen_date=2021-01-31
- QUERY CONDITION: select * from users where last_examen_date >= :from_date AND last_examen_date <: to_date
- PARAMETER: from_date = from.last_examen_date - to_date=to.last_examen_date

## prefixes
- from, to, like, obj, not, nil

## pagination parameters:
- startRow: (default is 0)
- pageSize: (default is 10)

## order 
- orderBy
