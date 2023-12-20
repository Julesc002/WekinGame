import axios from "axios";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { API_URL } from '../config';
import MDEditor from "@uiw/react-md-editor";

function DetailEntree() {
    const { id } = useParams();
    const { wikiid }=useParams();
    const [entry, setEntry] = useState([]);
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    useEffect(() => {
        searchDataEntry(id);
    }, [id]);

    const searchDataEntry = (id) => {
        axios.get(`${API_URL}/entry/` + id).then((res) => {
            setEntry(res.data);
        });
    };

    return (
        <div>
            {entry && entry[0] && (
                <div>
                    <h2 class="MainTitle">
                        Entrées du wiki&nbsp;
                        <Link to={`/wiki/${entry[0].id_wiki}`}>
                            {entry[0].nom_wiki}
                        </Link>
                        &nbsp;:
                    </h2>
                    <h2>{entry[0].nom}</h2>
                    <div class="append">
                        <h3>Catégories</h3>
                        {entry[0].categories.map((categorie) => (
                            <Link to={`/categorie/${entry[0].id_wiki}/${categorie}`}>
                                <p class="append">{categorie}</p>
                            </Link>
                        ))}
                        {entry[0].donnees.map((donnee, index) => (
                            <div key={index}>
                                <h3>{donnee.titre}</h3>
                                <MDEditor.Markdown source={donnee.contenu}/>
                            </div>
                        ))}
                    </div>
                </div>
            )}
            <button onClick={handleRetourClick}>Retour</button>
        </div>
    );
}

export default DetailEntree;