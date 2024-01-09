import MDEditor from "@uiw/react-md-editor";
import axios from "axios";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { API_URL } from '../config';
import BackgroundWiki from "./BackgroundWiki";

function DetailEntree() {
    const { id } = useParams();
    const [entry, setEntry] = useState({});
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    useEffect(() => {
        searchDataEntry(id);
    }, [id]);

    const searchDataEntry = (id) => {
        axios.get(`${API_URL}/entry/` + id).then((res) => {
            console.log(res.data);
            setEntry(res.data);
        });
    };
    return (
        <div>
            <BackgroundWiki id={entry.id_wiki} />
            {entry && entry._id && (
                <div>
                    <h2 class="MainTitle">
                        Entrées du wiki&nbsp;
                        <Link to={`/wiki/${entry.id_wiki}`}>
                            {entry.nom_wiki}
                        </Link>
                        &nbsp;:
                    </h2>
                    <h2>{entry.nom}</h2>
                    <div class="append">
                        <h3>Catégories</h3>
                        {entry.categories.map((categorie) => (
                            <Link to={`/categorie/${entry.id_wiki}/${categorie}`}>
                                <p class="append">{categorie}</p>
                            </Link>
                        ))}
                        {entry.donnees.map((donnee, index) => (
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